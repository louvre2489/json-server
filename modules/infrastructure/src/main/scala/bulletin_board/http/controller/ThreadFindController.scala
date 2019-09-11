package bulletin_board.http.controller

import akka.http.scaladsl.model.{ ContentType, HttpEntity, HttpResponse, MediaTypes, StatusCodes }
import akka.http.scaladsl.server.{ ExceptionHandler, Route }
import bulletin_board.application.usecase.ThreadFindUseCaseImpl
import bulletin_board.db.dao.{ PostDao, TagDao, ThreadDao, TokenDao }
import bulletin_board.domain.{ Post, Tags, Thread }
import bulletin_board.domain.value.{ PostId, ThreadId, Token }
import bulletin_board.model.ThreadFindModel.{ ThreadFindRequest, ThreadFindResponse }
import bulletin_board.model.{ PostRepository, TagRepository, ThreadRepository, TokenRepository }
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.{ Content, Schema }
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import javax.ws.rs._
import io.circe.generic.auto._
import io.circe.syntax._

object ThreadFindController {

  def apply() = new ThreadFindController()
}

@Path("/api/v1/threads/{threadId}")
@Consumes(Array("application/json"))
@Produces(Array("application/json"))
class ThreadFindController extends V1Controller {

  def route(implicit exceptionHandler: ExceptionHandler): Route = find

  @GET
  @Operation(
    summary = "Thread Find",
    description = "",
    requestBody = new RequestBody(content = Array(new Content(schema = new Schema()))),
    responses = Array(
      new ApiResponse(
        responseCode = "200",
        description = "Find Thread",
        content = Array(new Content(schema = new Schema(implementation = classOf[ThreadFindResponse])))
      ),
      new ApiResponse(responseCode = "400", description = "Invalid Parameter"),
      new ApiResponse(responseCode = "422", description = "Rule Violation"),
      new ApiResponse(responseCode = "500", description = "Internal server error")
    )
  )
  def find(implicit exceptionHandler: ExceptionHandler): Route =
    path(pathV1 / THREAD / LongNumber) { threadId =>
      get {
        headerValueByName("accessToken") { token: String =>
          handleExceptions(exceptionHandler) {
            // DAO
            implicit val tagRepository: TagRepository[Tags]                   = new TagDao()
            implicit val threadRepository: ThreadRepository[Thread, ThreadId] = new ThreadDao()
            implicit val postRepository: PostRepository[Post, PostId]         = new PostDao()
            implicit val tokenRepository: TokenRepository                     = new TokenDao()

            // Use Case
            val threadFindUseCase = new ThreadFindUseCaseImpl()

            val req = ThreadFindRequest(ThreadId(threadId))

            // 実行結果
            val threadFindResponse = threadFindUseCase.findThread(Token(token), req)

            threadFindResponse match {
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
