package bulletin_board.http.marshaller

import bulletin_board.domain.value.{ PlainPassword, UserId }
import bulletin_board.model.LoginModel.LoginRequest
import cats.data.Validated.{ Invalid, Valid }
import io.circe.parser._
import io.circe.{ Decoder, HCursor }
import org.slf4j.Logger

object LoginJsonDecoder {

  private val USER_ID = "userId"

  private val PASSWORD = "password"

  private val ACCESS_TOKEN = "accessToken"

  def jsonToLogin(stringJson: String)(implicit logger: Logger): Either[String, LoginRequest] = {

    implicit def decoder: io.circe.Decoder[LoginRequest] = Decoder.instance[LoginRequest] { (c: HCursor) =>
      for {
        userId   <- c.downField(USER_ID).as[Long]
        password <- c.downField(PASSWORD).as[String]
      } yield LoginRequest(UserId(userId), PlainPassword(password))
    }

    decodeAccumulating(stringJson) match {
      case Valid(value) => Right(value)
      case Invalid(e) => {
        logger.error(s"不正なリクエスト : ${this.getClass.getName} : ${stringJson} - ${e.toString()}")

        Left(stringJson)
      }

    }
  }
}
