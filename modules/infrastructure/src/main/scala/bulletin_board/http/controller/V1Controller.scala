package bulletin_board.http.controller

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{ HttpRequest, StatusCodes }
import akka.http.scaladsl.server.{ Directives, ExceptionHandler, PathMatcher, Route, StandardRoute }
import akka.stream.ActorMaterializer
import akka.util.ByteString
import bulletin_board.common._
import bulletin_board.util.BulletinBoardLogFactory
import org.slf4j.Logger

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

trait V1Controller extends Directives with SprayJsonSupport {

  /**
    * ロガー
    */
  implicit val logger: Logger = BulletinBoardLogFactory.logger

  /**
    * APIパスの共通部分
    */
  val pathV1: PathMatcher[Unit] = API / V1

  /**
    * 業務エラー時のレスポンス
    * @param ex `DomainException`オブジェクト
    * @return
    */
  def createDomainExceptionResponse(ex: DomainException): StandardRoute = {

    logger.error(s"業務エラー ： ${ex.message}")

    ex.exceptionType match {
      case ParameterException => complete(StatusCodes.BadRequest, ex.message)
      case RuleException      => complete(StatusCodes.UnprocessableEntity, ex.message)
      case DuplicateException => complete(StatusCodes.Conflict, ex.message)
      case _                  => complete(StatusCodes.InternalServerError, ex.message)
    }
  }

  /**
    * リクエストからjson文字列を抽出
    * @param request
    * @return
    */
  def extractRequest(request: HttpRequest)(implicit materializer: ActorMaterializer,
                                           executionContext: ExecutionContext): Future[String] =
    request.entity.dataBytes
      .runFold(ByteString.empty)(_ ++ _)
      .map(_.utf8String)

  /**
    * json文字列を抽出して業務処理を実行する
    * @param run
    * @param materializer
    * @param executionContext
    * @param exceptionHandler
    * @tparam A
    * @return
    */
  def jsonExtractor[A](decoder: String => Either[String, A])(run: A => Route)(
      implicit materializer: ActorMaterializer,
      executionContext: ExecutionContext,
      exceptionHandler: ExceptionHandler
  ): Route = {
    handleExceptions(exceptionHandler) {
      extract(ctx => ctx.request) { request =>
        onComplete {
          for {
            // 指定されたdecoreを使ってjsonから変換
            result <- extractRequest(request).map(decoder)
          } yield result
        } {
          // 変換成功時
          case Success(json) => {
            json match {
              // 変換成功時
              case Right(entity) => run(entity)

              // 変換失敗時
              case Left(errorJson) =>
                complete(
                  StatusCodes.BadRequest -> s"不正なリクエストです : ${errorJson}"
                )
            }
          }
          case Failure(e) => failWith(e)
        }
      }
    }
  }
}
