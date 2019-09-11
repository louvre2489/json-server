package bulletin_board.http.controller

import akka.http.scaladsl.model.{ ContentType, HttpEntity, HttpResponse, MediaTypes, StatusCodes }
import akka.http.scaladsl.server.{ ExceptionHandler, Route }
import akka.stream.ActorMaterializer
import bulletin_board.application.usecase.ThreadDeleteUseCaseImpl
import bulletin_board.db.dao.{ TagDao, ThreadDao, TokenDao }
import bulletin_board.domain.{ Tags, Thread }
import bulletin_board.domain.value.{ ThreadId, Token }
import bulletin_board.model.ThreadDeleteModel.{ ThreadDeleteRequest, ThreadDeleteResponse }
import bulletin_board.model.{ TagRepository, ThreadRepository, TokenRepository }
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.{ Content, Schema }
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import javax.ws.rs._
import io.circe.generic.auto._
import io.circe.syntax._

import scala.concurrent.ExecutionContext

object ThreadDeleteController {

  def apply()(implicit materializer: ActorMaterializer, executionContext: ExecutionContext) =
    new ThreadDeleteController()
}

@Path("/api/v1/threads")
@Consumes(Array("application/json"))
@Produces(Array("application/json"))
class ThreadDeleteController(implicit materializer: ActorMaterializer, executionContext: ExecutionContext)
    extends V1Controller {

  def route(implicit exceptionHandler: ExceptionHandler): Route = deleteThread

  @DELETE
  @Operation(
    summary = "Thread Delete",
    description = "",
    requestBody =
      new RequestBody(content = Array(new Content(schema = new Schema(implementation = classOf[ThreadDeleteRequest])))),
    responses = Array(
      new ApiResponse(
        responseCode = "200",
        description = "Delete Success",
        content = Array(new Content(schema = new Schema(implementation = classOf[ThreadDeleteResponse])))
      ),
      new ApiResponse(responseCode = "400", description = "Invalid Parameter"),
      new ApiResponse(responseCode = "422", description = "Rule Violation"),
      new ApiResponse(responseCode = "500", description = "Internal server error")
    )
  )
  def deleteThread(implicit exceptionHandler: ExceptionHandler): Route =
    path(pathV1 / THREAD / LongNumber) { threadId =>
      delete {
        headerValueByName("accessToken") { token: String =>
          handleExceptions(exceptionHandler) {
            // DAO
            implicit val tagRepository: TagRepository[Tags]                   = new TagDao()
            implicit val threadRepository: ThreadRepository[Thread, ThreadId] = new ThreadDao()
            implicit val tokenRepository: TokenRepository                     = new TokenDao()

            // Use Case
            val threadDeleteUseCase = new ThreadDeleteUseCaseImpl()

            val req = ThreadDeleteRequest(ThreadId(threadId.toLong))

            // 実行結果
            val threadDeleteResponse = threadDeleteUseCase.deleteThread(Token(token), req)

            threadDeleteResponse match {
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
