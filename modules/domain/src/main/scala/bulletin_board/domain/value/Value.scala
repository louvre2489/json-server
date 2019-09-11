package bulletin_board.domain.value

import java.util.Date

trait Value[T] extends Any {
  def value: T
}

/**
  * User関連
  */
sealed trait Password                    extends Any
case class PlainPassword(value: String)  extends AnyVal with Value[String] with Password
case class HashedPassword(value: String) extends AnyVal with Value[String] with Password

case class MailAddress(value: String) extends AnyVal with Value[String]
case class UserName(value: String)    extends AnyVal with Value[String]

/**
  * Thread関連
  */
case class ThreadTitle(value: String)    extends AnyVal with Value[String]
case class ThreadCreatedAt(value: Date)  extends AnyVal with Value[Date]
case class ThreadLastPostAt(value: Date) extends AnyVal with Value[Date]

/**
  * Post関連
  * @param value
  */
case class PostContent(value: String) extends AnyVal with Value[String]
case class PostCreatedAt(value: Date) extends AnyVal with Value[Date]
