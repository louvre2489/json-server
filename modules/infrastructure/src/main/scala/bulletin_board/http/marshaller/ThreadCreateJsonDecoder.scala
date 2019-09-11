package bulletin_board.http.marshaller

import bulletin_board.domain.value.ThreadTitle
import bulletin_board.domain.{ Tag, Tags }
import bulletin_board.model.ThreadCreateModel.ThreadCreateRequest
import cats.data.Validated.{ Invalid, Valid }
import io.circe.parser._
import io.circe.{ Decoder, HCursor }
import org.slf4j.Logger

object ThreadCreateJsonDecoder {

  private val TITLE = "title"

  private val TAGS = "tags"

  def jsonToThreadCreate(stringJson: String)(implicit logger: Logger): Either[String, ThreadCreateRequest] = {

    implicit def decoder: io.circe.Decoder[ThreadCreateRequest] = Decoder.instance[ThreadCreateRequest] {
      (c: HCursor) =>
        for {
          title <- c.downField(TITLE).as[String]
          tags  <- c.downField(TAGS).as[Array[String]]
        } yield ThreadCreateRequest(ThreadTitle(title), arrayStringToTag(tags))
    }

    implicit def arrayStringToTag(tags: Array[String]): Array[Tag] = tags.map(Tag(_))

    decodeAccumulating(stringJson) match {
      case Valid(value) => Right(value)
      case Invalid(e) => {
        logger.error(s"不正なリクエスト : ${this.getClass.getName} : ${stringJson} - ${e.toString()}")

        Left(stringJson)
      }

    }
  }
}
