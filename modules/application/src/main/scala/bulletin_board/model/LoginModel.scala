package bulletin_board.model

import bulletin_board.domain.value.{ PlainPassword, UserId }

object LoginModel {
  case class LoginRequest(userId: UserId, password: PlainPassword) {}
  case class LoginResponse(accessToken: String)                    {}
}
