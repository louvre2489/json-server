package bulletin_board.http.controller

import akka.http.scaladsl.model.{ ContentType, HttpEntity, HttpResponse, MediaTypes, StatusCodes }
import akka.http.scaladsl.server.{ ExceptionHandler, Route }
import akka.stream.ActorMaterializer
import bulletin_board.application.usecase.LoginUseCaseImpl
import bulletin_board.db.dao.{ TokenDao, UserDao }
import bulletin_board.domain.User
import bulletin_board.domain.value.UserId
import bulletin_board.http.marshaller.LoginJsonDecoder
import bulletin_board.model.LoginModel.{ LoginRequest, LoginResponse }
import bulletin_board.model.{ TokenRepository, UserRepository }
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.{ Content, Schema }
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import javax.ws.rs._
import io.circe.generic.auto._
import io.circe.syntax._

import scala.concurrent.ExecutionContext

object LoginController {

  def apply()(implicit materializer: ActorMaterializer, executionContext: ExecutionContext) = new LoginController()
}

@Path("/api/v1/login")
@Consumes(Array("application/json"))
@Produces(Array("application/json"))
class LoginController(implicit materializer: ActorMaterializer, executionContext: ExecutionContext)
    extends V1Controller {

  def route(implicit exceptionHandler: ExceptionHandler): Route = login

  @POST
  @Operation(
    summary = "Login",
    description = "",
    requestBody =
      new RequestBody(content = Array(new Content(schema = new Schema(implementation = classOf[LoginRequest])))),
    responses = Array(
      new ApiResponse(responseCode = "200",
                      description = "Login Success",
                      content = Array(new Content(schema = new Schema(implementation = classOf[LoginResponse])))),
      new ApiResponse(responseCode = "400", description = "Invalid Parameter"),
      new ApiResponse(responseCode = "422", description = "Rule Violation"),
      new ApiResponse(responseCode = "500", description = "Internal server error")
    )
  )
  def login(implicit exceptionHandler: ExceptionHandler): Route =
    path(pathV1 / LOGIN) {
      post {
        jsonExtractor[LoginRequest](LoginJsonDecoder.jsonToLogin) { entity =>
          // DAO
          implicit val userRepository: UserRepository[User, UserId] = new UserDao()
          implicit val tokenRepository: TokenRepository             = new TokenDao()

          // Use Case
          val loginUseCase = new LoginUseCaseImpl()

          // 実行結果
          val userCreateResponse = loginUseCase.login(entity)

          userCreateResponse match {
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
