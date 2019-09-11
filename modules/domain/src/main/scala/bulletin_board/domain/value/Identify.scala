package bulletin_board.domain.value

sealed trait Identify extends Any

case class MessageId(value: String) extends AnyVal with Value[String] with Identify

case class ThreadId(value: Long) extends AnyVal with Value[Long] with Identify

case class PostId(value: Long) extends AnyVal with Value[Long] with Identify

case class UserId(value: Long) extends AnyVal with Value[Long] with Identify

case class Token(value: String) extends AnyVal with Value[String] with Identify
