package bulletin_board.http.controller

import akka.http.scaladsl.model.{ ContentType, HttpEntity, HttpResponse, MediaTypes, StatusCodes }
import akka.http.scaladsl.server.{ ExceptionHandler, Route }
import bulletin_board.application.usecase.PostFindUseCaseImpl
import bulletin_board.model.PostFindModel.PostFindRequest
import bulletin_board.db.dao.{ PostDao, TagDao, ThreadDao, TokenDao }
import bulletin_board.domain.{ Post, Tags, Thread }
import bulletin_board.domain.value.{ PostId, ThreadId, Token }
import bulletin_board.model.PostFindModel.PostFindResponse
import bulletin_board.model.{ PostRepository, TagRepository, ThreadRepository, TokenRepository }
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.{ Content, Schema }
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import javax.ws.rs._
import io.circe.generic.auto._
import io.circe.syntax._

object PostFindController {

  def apply() = new PostFindController()
}

@Path("/api/v1/threads/{threadId}/posts/{postId}")
@Consumes(Array("application/json"))
@Produces(Array("application/json"))
class PostFindController extends V1Controller {

  def route(implicit exceptionHandler: ExceptionHandler): Route = find

  @GET
  @Operation(
    summary = "Post Find",
    description = "",
    requestBody = new RequestBody(content = Array(new Content(schema = new Schema()))),
    responses = Array(
      new ApiResponse(
        responseCode = "200",
        description = "Find Success",
        content = Array(new Content(schema = new Schema(implementation = classOf[PostFindResponse])))
      ),
      new ApiResponse(responseCode = "400", description = "Invalid Parameter"),
      new ApiResponse(responseCode = "422", description = "Rule Violation"),
      new ApiResponse(responseCode = "500", description = "Internal server error")
    )
  )
  def find(implicit exceptionHandler: ExceptionHandler): Route =
    path(pathV1 / THREAD / LongNumber / POST / LongNumber) { (threadId, postId) =>
      get {
        headerValueByName("accessToken") { token: String =>
          handleExceptions(exceptionHandler) {
            // DAO
            implicit val tagRepository: TagRepository[Tags]                   = new TagDao()
            implicit val threadRepository: ThreadRepository[Thread, ThreadId] = new ThreadDao()
            implicit val postRepository: PostRepository[Post, PostId]         = new PostDao()
            implicit val tokenRepository: TokenRepository                     = new TokenDao()

            // Use Case
            val postFindUseCase = new PostFindUseCaseImpl()

            val req = PostFindRequest(ThreadId(threadId), PostId(postId))

            // 実行結果
            val postFindResponse = postFindUseCase.findPost(Token(token), req)

            postFindResponse match {
              case Left(ex) =>
                // 業務エラー
                createDomainExceptionResponse(ex)

              case Right(response) =>
                // 正常終了
                complete(
                  // レスポンス
                  HttpResponse(
                    status = StatusCodes.OK,
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
