package bulletin_board.http

import java.io.{ PrintWriter, StringWriter }

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ ExceptionHandler, Route }
import akka.stream.ActorMaterializer
import akka.util.Timeout
import bulletin_board.http.controller._
import bulletin_board.util.BulletinBoardLogFactory
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import org.slf4j.Logger

import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal

object Routes {
  def apply()(implicit system: ActorSystem,
              materializer: ActorMaterializer,
              executionContext: ExecutionContext,
              timeout: Timeout): Routes =
    new Routes()
}

/**
  * Routing
  * @param system Actorシステム
  * @param timeout akka.util.Timeout
  */
class Routes()(implicit system: ActorSystem,
               materialize: ActorMaterializer,
               executionContext: ExecutionContext,
               timeout: Timeout)
    extends SprayJsonSupport {

  val logger: Logger = BulletinBoardLogFactory.logger

  def stackTrace(th: Throwable): String = {
    val sw = new StringWriter
    th.printStackTrace(new PrintWriter(sw))
    sw.toString
  }

  /** *
    * エラーハンドラー
    */
  implicit def customExceptionHandler: ExceptionHandler = ExceptionHandler {
    case NonFatal(ex) =>
      extractUri { uri =>
        val errorMessage =
          s"NonFatalError!! URI: ${uri}, Reason: ${ex.getMessage}, StackTrace: ${stackTrace(ex)}"

        // 例外内容をログファイルに出力
        logger.error(errorMessage)

        complete(StatusCodes.InternalServerError -> "システム管理者に連絡してください。")
      }
    case ex => {
      extractUri { uri =>
        val errorMessage =
          s"FatalError!! URI: ${uri}, Reason: ${ex.getMessage}, StackTrace: ${stackTrace(ex)}"

        // 例外内容をログファイルに出力
        logger.error(errorMessage)

        complete(StatusCodes.InternalServerError -> "システム管理者に連絡してください。")
      }
    }
  }

  val routes: Route = (cors() | handleExceptions(customExceptionHandler)) { // Swaggerを利用するためにCORS対応
    extractUri { uri =>
      extractMethod { method =>
        // APIへのアクセスをロギング
        logger.info(s"Api called. method: ${method.value}, uri: ${uri}")

        /**
          * Swagger起動パス
          *
          * @return
          */
        def swaggerRoute: Route = path("swagger") {
          getFromResource("swagger/index.html")
        }

        /**
          * Swagger リソース公開用ディレクトリ
          *
          * @return
          */
        def swaggerDirectory: Route =
          getFromResourceDirectory("swagger")

        /**
          * swagger.yaml/swagger.json生成パス
          *
          * @return
          */
        def swaggerDoc: Route =
          new SwaggerDocService(
            Set(
              classOf[LoginController],
              classOf[UserCreateController],
              classOf[ThreadCreateController],
              classOf[ThreadDeleteController],
              classOf[ThreadFindController],
              classOf[PostCreateController],
              classOf[PostFindController]
            )
          ).routes

        swaggerRoute ~
        swaggerDirectory ~
        swaggerDoc ~
        UserCreateController().route ~
        LoginController().route ~
        ThreadCreateController().route ~
        ThreadDeleteController().route ~
        ThreadFindController().route ~
        PostCreateController().route ~
        PostFindController().route
      }
    }
  }
}
