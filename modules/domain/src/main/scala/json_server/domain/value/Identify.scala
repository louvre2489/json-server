package json_server.domain.value

sealed trait Identify extends Any

case class MessageId(value: String) extends AnyVal with Value[String] with Identify

case class MyId(value: Long) extends AnyVal with Value[Long] with Identify
