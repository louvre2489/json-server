package bulletin_board.application.usecase

import bulletin_board.common.{ DomainException, RuleException }
import bulletin_board.domain.User
import bulletin_board.domain.value.{ MessageId, UserId }
import bulletin_board.model.UserCreateModel.{ UserCreateRequest, UserCreateResponse }
import bulletin_board.model.UserRepository
import org.slf4j.Logger

trait UserCreateUseCase {

  def createUser(
      user: UserCreateRequest
  )(userRepository: UserRepository[User, UserId])(implicit logger: Logger): Either[DomainException, UserCreateResponse]
}

class UserCreateUseCaseImpl extends UserCreateUseCase {

  def createUser(
      userRequest: UserCreateRequest
  )(
      userRepository: UserRepository[User, UserId]
  )(implicit logger: Logger): Either[DomainException, UserCreateResponse] = {

    try {

      val user = User(None, userRequest.mailAddress, userRequest.userName, userRequest.password)

      // ハッシュ化されたパスワード
      val hashedPassword = user.createHashedPassword(user.password)

      // 保存
      val createResult = userRepository.save(user, hashedPassword)

      createResult match {
        case Left(e) => Left(e)
        case Right(user) =>
          Right(
            UserCreateResponse(userId = user.userId.getOrElse(UserId(-1)).value,
                               userName = user.userName.value,
                               mailAddress = user.mailAddress.value)
          )
      }
    } catch {
      //　Domainの引数チェックで発生する例外
      case e: IllegalArgumentException => {
        val removedMessagePart = "requirement failed: "
        Left(DomainException(RuleException, MessageId(e.getMessage.replace(removedMessagePart, ""))))
      }
      case e: Throwable => throw e
    }
  }
}
