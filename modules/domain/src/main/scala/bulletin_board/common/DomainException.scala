package bulletin_board.common

import bulletin_board.domain.value.MessageId

/**
  * 業務エラータイプ
  */
sealed trait ExceptionType

/**
  * 入力値不正
  */
object ParameterException extends ExceptionType

/**
  * 業務ルール違反
  */
object RuleException extends ExceptionType

/**
  * 重複エラー
  */
object DuplicateException extends ExceptionType

/**
  * 想定外のエラー
  */
object FatalException extends ExceptionType

/**
  * 業務例外
  * @param exceptionType 例外の種類を設定する。これによってレスポンスのステータスを切り替える
  * @param message エラー
  */
case class DomainException(exceptionType: ExceptionType, message: String) extends Exception(message)

object DomainException {

  def apply(exceptionType: ExceptionType, messageId: MessageId, params: Seq[String] = Seq()): DomainException = {

    MessageFactory.getMessage(messageId, params)

    var message = MessageFactory.getMessage(messageId)

    params.zipWithIndex.foreach {
      case (param: String, i: Int) => {
        message = message.replace("{" + i.toString() + "}", param)
      }
    }

    DomainException(exceptionType, message)
  }
}
