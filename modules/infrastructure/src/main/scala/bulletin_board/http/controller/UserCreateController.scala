package bulletin_board.http.controller

import akka.http.scaladsl.server.ExceptionHandler
import akka.http.scaladsl.model.{ ContentType, HttpEntity, HttpResponse, MediaTypes, StatusCodes }
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import bulletin_board.application.usecase.UserCreateUseCaseImpl
import bulletin_board.db.dao.UserDao
import bulletin_board.domain.User
import bulletin_board.domain.value.UserId
import bulletin_board.http.marshaller.UserCreateJsonDecoder
import bulletin_board.model.UserCreateModel.{ UserCreateRequest, UserCreateResponse }
import bulletin_board.model.UserRepository
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.{ Content, Schema }
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import javax.ws.rs._
import io.circe.generic.auto._
import io.circe.syntax._

import scala.concurrent.ExecutionContext

object UserCreateController {

  def apply()(implicit materializer: ActorMaterializer, executionContext: ExecutionContext) = new UserCreateController()
}

@Path("/api/v1/user/create")
@Consumes(Array("application/json"))
@Produces(Array("application/json"))
class UserCreateController(implicit materializer: ActorMaterializer, executionContext: ExecutionContext)
    extends V1Controller {

  def route(implicit exceptionHandler: ExceptionHandler): Route = create

  @POST
  @Operation(
    summary = "User Creation",
    description = "",
    requestBody =
      new RequestBody(content = Array(new Content(schema = new Schema(implementation = classOf[UserCreateRequest])))),
    responses = Array(
      new ApiResponse(
        responseCode = "201",
        description = "Creation Success",
        content = Array(new Content(schema = new Schema(implementation = classOf[UserCreateResponse])))
      ),
      new ApiResponse(responseCode = "400", description = "Invalid Parameter"),
      new ApiResponse(responseCode = "422", description = "Rule Violation"),
      new ApiResponse(responseCode = "500", description = "Internal server error")
    )
  )
  def create(implicit exceptionHandler: ExceptionHandler): Route =
    path(pathV1 / USER / CREATE) {
      post {
        jsonExtractor[UserCreateRequest](UserCreateJsonDecoder.jsonToUserCreate) { entity =>
          val userRepository: UserRepository[User, UserId] = new UserDao()

          // Use Case
          val userCreateUseCase = new UserCreateUseCaseImpl()

          // 実行結果
          val userCreateResponse = userCreateUseCase.createUser(entity)(userRepository)

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
