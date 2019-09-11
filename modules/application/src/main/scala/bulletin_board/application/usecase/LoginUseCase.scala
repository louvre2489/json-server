package bulletin_board.application.usecase

import java.util.Date

import bulletin_board.common.{ DomainException, ParameterException }
import bulletin_board.domain.User
import bulletin_board.domain.value.{ MessageId, Token, UserId }
import bulletin_board.model.LoginModel.{ LoginRequest, LoginResponse }
import bulletin_board.model.{ TokenRepository, UserRepository }
import org.slf4j.Logger

trait LoginUseCase {

  def login(
      loginRequest: LoginRequest
  )(implicit logger: Logger,
    userRepository: UserRepository[User, UserId],
    tokenRepository: TokenRepository): Either[DomainException, LoginResponse]
}

class LoginUseCaseImpl extends LoginUseCase {

  def login(
      loginRequest: LoginRequest
  )(implicit logger: Logger,
    userRepository: UserRepository[User, UserId],
    tokenRepository: TokenRepository): Either[DomainException, LoginResponse] = {

    // ユーザー検索
    val userOption = userRepository.findById(loginRequest.userId)

    userOption match {
      case None =>
        // ユーザーが存在しない場合はエラー
        Left(DomainException(ParameterException, MessageId("NOT_EXISTS")))
      case Some(u) => {

        // APIから渡された平文パスワードを`User`に設定
        val user = u.copy(password = loginRequest.password)

        // DBに保存されているパスワード(ハッシュ化済)を取得する
        val hashedPassword = userRepository.getPassword(user.userId.getOrElse(UserId(-1)))

        // パスワードチェック
        if (user.matchingPassword(user.password, hashedPassword)) {

          // パスワードが一致したらトークンを発行
          val token = createToken(user.userId.getOrElse(UserId(-1)))
          tokenRepository.createToken(token, user.userId.getOrElse(UserId(-1)))

          Right(LoginResponse(token.value))
        } else
          Left(DomainException(ParameterException, MessageId("NOT_EXISTS")))
      }
    }
  }

  private def createToken(userId: UserId): Token = {

    // 現在時刻を文字列化して作成
    Token(userId.value + "%tY%<tm%<td%<tH%<tM%<tS%<tL" format new Date)
  }

}
