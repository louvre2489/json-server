package bulletin_board.common

import com.typesafe.config.{ Config, ConfigFactory }
import bulletin_board.domain.value.MessageId

object MessageFactory {

  def load: Config = {

    val conf = ConfigFactory.load()
    ConfigFactory.load(conf.getString(MESSAGE_FILE))
  }

  def getMessage(messageId: MessageId): String = {

    val messageConf = this.load
    messageConf.getString(messageId.value)
  }

  def getMessage(messageId: MessageId, params: Seq[String]): String = {

    var message = MessageFactory.getMessage(messageId)

    params.zipWithIndex.foreach {
      case (param: String, i: Int) => {
        message = message.replace("{" + i.toString() + "}", param)
      }
    }

    message
  }
}
