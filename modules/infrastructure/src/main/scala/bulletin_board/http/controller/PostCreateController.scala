package bulletin_board.http.controller

import akka.http.scaladsl.model.{ ContentType, HttpEntity, HttpResponse, MediaTypes, StatusCodes }
import akka.http.scaladsl.server.{ ExceptionHandler, Route }
import akka.stream.ActorMaterializer
import bulletin_board.application.usecase.PostCreateUseCaseImpl
import bulletin_board.db.dao.{ PostDao, TagDao, ThreadDao, TokenDao }
import bulletin_board.domain.{ Post, Tags, Thread }
import bulletin_board.domain.value.{ PostId, ThreadId, Token }
import bulletin_board.http.marshaller.PostCreateJsonDecoder
import bulletin_board.model.PostCreateModel.{ PostCreateRequest, PostCreateResponse }
import bulletin_board.model.{ PostRepository, TagRepository, ThreadRepository, TokenRepository }
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.{ Content, Schema }
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import javax.ws.rs._
import io.circe.generic.auto._
import io.circe.syntax._

import scala.concurrent.ExecutionContext

object PostCreateController {

  def apply()(implicit materializer: ActorMaterializer, executionContext: ExecutionContext) = new PostCreateController()
}

@Path("/api/v1/threads/{threadId}/posts/{postId}")
@Consumes(Array("application/json"))
@Produces(Array("application/json"))
class PostCreateController(implicit materializer: ActorMaterializer, executionContext: ExecutionContext)
    extends V1Controller {

  def route(implicit exceptionHandler: ExceptionHandler): Route = create

  @POST
  @Operation(
    summary = "Post Create",
    description = "",
    requestBody =
      new RequestBody(content = Array(new Content(schema = new Schema(implementation = classOf[PostCreateRequest])))),
    responses = Array(
      new ApiResponse(
        responseCode = "200",
        description = "Creation Success",
        content = Array(new Content(schema = new Schema(implementation = classOf[PostCreateResponse])))
      ),
      new ApiResponse(responseCode = "400", description = "Invalid Parameter"),
      new ApiResponse(responseCode = "422", description = "Rule Violation"),
      new ApiResponse(responseCode = "500", description = "Internal server error")
    )
  )
  def create(implicit exceptionHandler: ExceptionHandler): Route =
    path(pathV1 / THREAD / LongNumber / POST) { threadId =>
      post {
        headerValueByName("accessToken") { token: String =>
          jsonExtractor[PostCreateRequest](PostCreateJsonDecoder.jsonToPostCreate(threadId)) { entity =>
            // DAO
            implicit val tagRepository: TagRepository[Tags]                   = new TagDao()
            implicit val threadRepository: ThreadRepository[Thread, ThreadId] = new ThreadDao()
            implicit val postRepository: PostRepository[Post, PostId]         = new PostDao()
            implicit val tokenRepository: TokenRepository                     = new TokenDao()

            // Use Case
            val postCreateUseCase = new PostCreateUseCaseImpl()

            // 実行結果
            val postCreateResponse = postCreateUseCase.createPost(Token(token), entity)

            postCreateResponse match {
              case Left(ex) =>
                // 業務エラー
                createDomainExceptionResponse(ex)

              case Right(response) =>
                // 正常終了
                complete(
                  // レスポンス
                  HttpResponse(
                    status = StatusCodes.Created,
                    entity = HttpEntity(
                      ContentType(MediaTypes.`application/json`),
                      response.asJson.noSpaces
                    )
                  )
                )
            }
          }
        }
      }
    }
}
