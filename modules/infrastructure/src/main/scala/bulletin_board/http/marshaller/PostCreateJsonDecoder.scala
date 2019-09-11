package bulletin_board.http.marshaller

import bulletin_board.domain.value.{ PostContent, ThreadId }
import bulletin_board.model.PostCreateModel.PostCreateRequest
import cats.data.Validated.{ Invalid, Valid }
import io.circe.parser._
import io.circe.{ Decoder, HCursor }
import org.slf4j.Logger

object PostCreateJsonDecoder {

  private val CONTENT = "content"

  def jsonToPostCreate(threadId: Long)(
      stringJson: String
  )(implicit logger: Logger): Either[String, PostCreateRequest] = {

    implicit def decoder: io.circe.Decoder[PostCreateRequest] = Decoder.instance[PostCreateRequest] { (c: HCursor) =>
      for {
        content <- c.downField(CONTENT).as[String]
      } yield PostCreateRequest(ThreadId(threadId), PostContent(content))
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
