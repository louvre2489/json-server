package bulletin_board.model

import bulletin_board.domain.value.{ MailAddress, PlainPassword, UserName }

object UserCreateModel {
  case class UserCreateRequest(mailAddress: MailAddress, password: PlainPassword, userName: UserName) {}
  case class UserCreateResponse(userId: Long, userName: String, mailAddress: String)                  {}
}
