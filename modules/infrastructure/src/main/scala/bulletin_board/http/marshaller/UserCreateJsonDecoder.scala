package bulletin_board.http.marshaller

import bulletin_board.domain.value.{ MailAddress, PlainPassword, UserName }
import bulletin_board.model.UserCreateModel.UserCreateRequest
import cats.data.Validated.{ Invalid, Valid }
import io.circe.{ Decoder, HCursor }
import io.circe.parser._
import org.slf4j.Logger

object UserCreateJsonDecoder {

  private val MAIL_ADDRESS = "mailAddress"

  private val USER_NAME = "userName"

  private val PASSWORD = "password"

  def jsonToUserCreate(stringJson: String)(implicit logger: Logger): Either[String, UserCreateRequest] = {

    implicit def decoder: io.circe.Decoder[UserCreateRequest] = Decoder.instance[UserCreateRequest] { (c: HCursor) =>
      for {
        mailAddress <- c.downField(MAIL_ADDRESS).as[String]
        userName    <- c.downField(USER_NAME).as[String]
        password    <- c.downField(PASSWORD).as[String]
      } yield UserCreateRequest(MailAddress(mailAddress), PlainPassword(password), UserName(userName))
    }

    decodeAccumulating(stringJson) match {
      case Valid(value) => Right(value)
      case Invalid(_) => {
        logger.error(s"不正なリクエスト : ${this.getClass.getName} : ${stringJson}")

        Left(stringJson)
      }

    }
  }
}
