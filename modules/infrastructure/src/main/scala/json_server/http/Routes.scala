package json_server.http

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ ExceptionHandler, Route }
import akka.util.Timeout
import json_server.http.controller.ItemController
import scalikejdbc.{ ConnectionPool, DB }
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._

import scala.util.control.NonFatal

/**
  * Routing Object
  */
object Routes {

  def apply()(implicit system: ActorSystem, timeout: Timeout): Routes =
    new Routes()
}

/**
  * Routing
  */
class Routes()(implicit system: ActorSystem, timeout: Timeout) extends SprayJsonSupport {

//  private val conf: Config = ConfigFactory.load

  /***
    * Custom Error Handler
    */
  implicit def customExceptionHandler: ExceptionHandler = ExceptionHandler {
    case NonFatal(ex) =>
      extractLog { implicit log =>
        extractUri { uri =>
          val errorMessage = s"ERROR!! URI: $uri, REASON: ${ex.getMessage}"

          log.error(errorMessage)

          complete(StatusCodes.InternalServerError -> errorMessage)
        }
      }
  }

  /***
    * Routing
    */
  val routes: Route = (cors() | handleExceptions(customExceptionHandler)) {
    extractLog { implicit log =>
      extractUri { uri =>
        extractMethod { method =>
          log.info(s"call api. method: ${method.value}, uri: $uri")

          /**
            * コネクションの取得
            * @return
            */
          def borrow: DB = DB(ConnectionPool.borrow())

          /**
            * ローンパターンでアプリケーションを実行
            * @param db DB接続情報
            * @param application アプリケーション処理
            * @return
            */
          def using(db: DB)(application: DB => Route): Route = {
            try {
              application(db)
            } finally {
              db.close()
            }
          }

          path(FRONTEND_ROOT_PATH / Remaining) { resource =>
            pathEndOrSingleSlash {
              get {
                getFromResource(RESOURCE_ROOT_PATH + "/" + FRONTEND_ROOT_PATH + "/" + resource)
              }
            }

          } ~ path("test") {
            pathEndOrSingleSlash {
              get {

//                using(borrow) { implicit db =>
//                  implicit val systemListFormat = SystemJsonProtocol.systemListFormat
//
//                  val data = new com.louvre2489.fp.application.query.QuerySystem(
//                    com.louvre2489.fp.rdb.repository.SystemDao()
//                  ).findAll.map(s => System(s.systemId, s.systemName))
//
//                  complete(data)
//
                //               }
                complete("OK!!!!")
              }
            }
          } ~ path("swagger") {
            getFromResource("swagger/index.html")
          } ~ getFromResourceDirectory("swagger") ~
          new SwaggerDocService(Set(classOf[ItemController])).routes ~ new ItemController().route
        }
      }
    }
  }
}
