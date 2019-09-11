package bulletin_board.http.controller

import akka.http.scaladsl.model.{ ContentType, HttpEntity, HttpResponse, MediaTypes, StatusCodes }
import akka.http.scaladsl.server.{ ExceptionHandler, Route }
import akka.stream.ActorMaterializer
import bulletin_board.application.usecase.ThreadCreateUseCaseImpl
import bulletin_board.db.dao.{ TagDao, ThreadDao, TokenDao }
import bulletin_board.domain.{ Tags, Thread }
import bulletin_board.domain.value.{ ThreadId, Token }
import bulletin_board.http.marshaller.ThreadCreateJsonDecoder
import bulletin_board.model.ThreadCreateModel.{ ThreadCreateRequest, ThreadCreateResponse }
import bulletin_board.model.{ TagRepository, ThreadRepository, TokenRepository }
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.{ Content, Schema }
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import javax.ws.rs._
import io.circe.generic.auto._
import io.circe.syntax._

import scala.concurrent.ExecutionContext

object ThreadCreateController {

  def apply()(implicit materializer: ActorMaterializer, executionContext: ExecutionContext) =
    new ThreadCreateController()
}

@Path("/api/v1/threads")
@Consumes(Array("application/json"))
@Produces(Array("application/json"))
class ThreadCreateController(implicit materializer: ActorMaterializer, executionContext: ExecutionContext)
    extends V1Controller {

  def route(implicit exceptionHandler: ExceptionHandler): Route = create

  @POST
  @Operation(
    summary = "Thread Create",
    description = "",
    requestBody =
      new RequestBody(content = Array(new Content(schema = new Schema(implementation = classOf[ThreadCreateRequest])))),
    responses = Array(
      new ApiResponse(
        responseCode = "201",
        description = "Creation Success",
        content = Array(new Content(schema = new Schema(implementation = classOf[ThreadCreateResponse])))
      ),
      new ApiResponse(responseCode = "400", description = "Invalid Parameter"),
      new ApiResponse(responseCode = "422", description = "Rule Violation"),
      new ApiResponse(responseCode = "500", description = "Internal server error")
    )
  )
  def create(implicit exceptionHandler: ExceptionHandler): Route =
    path(pathV1 / THREAD) {
      post {
        headerValueByName("accessToken") { token: String =>
          jsonExtractor[ThreadCreateRequest](ThreadCreateJsonDecoder.jsonToThreadCreate) { entity =>
            // DAO
            implicit val tagRepository: TagRepository[Tags]                   = new TagDao()
            implicit val threadRepository: ThreadRepository[Thread, ThreadId] = new ThreadDao()
            implicit val tokenRepository: TokenRepository                     = new TokenDao()

            // Use Case
            val threadCreateUseCase = new ThreadCreateUseCaseImpl()

            // 実行結果
            val threadCreateResponse = threadCreateUseCase.createThread(Token(token), entity)

            threadCreateResponse match {
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
