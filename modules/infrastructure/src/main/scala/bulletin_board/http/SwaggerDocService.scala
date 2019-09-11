package bulletin_board.http

import com.github.swagger.akka.SwaggerHttpService
import com.typesafe.config.ConfigFactory

class SwaggerDocService(val apis: Set[Class[_]]) extends SwaggerHttpService {

  val conf = ConfigFactory.load

  val address = conf.getString(MY_HOST)
  val port    = conf.getInt(MY_PORT)

  override val apiClasses  = apis
  override val host        = s"$address:$port" //the url of your api, not swagger's json endpoint
  override val apiDocsPath = "api-docs" //where you want the swagger-json endpoint exposed
  override val schemes     = List("https", "http")
}
