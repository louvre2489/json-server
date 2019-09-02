package json_server.http.controller

import akka.http.scaladsl.model.{ ContentType, HttpEntity, HttpResponse, MediaTypes, StatusCodes }
import akka.http.scaladsl.server.{ Directives, Route }
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.{ Content, Schema }
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import javax.ws.rs._
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._

@Path("/items")
@Consumes(Array("application/json"))
@Produces(Array("application/json"))
class ItemController extends Directives {

  def route: Route = create

  @GET
  @Operation(
    summary = "Get Items",
    description = "",
    requestBody = new RequestBody(content = Array(new Content(schema = new Schema()))),
    responses = Array(
      new ApiResponse(responseCode = "200",
                      description = "Create response",
                      content = Array(new Content(schema = new Schema(implementation = classOf[ItemResponse])))),
      new ApiResponse(responseCode = "500", description = "Internal server error")
    )
  )
  def create: Route = path("items") {
    get {
      complete(
        HttpResponse(
          entity = HttpEntity(ContentType(MediaTypes.`application/json`), ItemResponse(1, "PEN").asJson.noSpaces)
        )
      )
    }
  }
}

case class ItemResponse(itemId: Int, ItemName: String) {}
