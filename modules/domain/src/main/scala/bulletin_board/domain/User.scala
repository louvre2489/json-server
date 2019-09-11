package bulletin_board.domain

import java.util.regex.Pattern

import bulletin_board.common._
import bulletin_board.domain.entity.Entity
import bulletin_board.domain.value.{ HashedPassword, MailAddress, PlainPassword, UserId, UserName }

object User {

  val userNameLength = 10

  val passwordLength = 10
}

case class User(userId: Option[UserId], mailAddress: MailAddress, userName: UserName, password: PlainPassword)
    extends Entity[User] {

  // メールアドレスのチェック
  {
    // 必須チェック
    require(isNonEmpty(mailAddress.value), MailAddressIsEmptyError)
    // メアドフォーマットチェック
    require(isValidMailAddress, MailAddressFormatError)
  }

  // ユーザー名のチェック
  {
    // 必須チェック
    require(isNonEmpty(userName.value), UserNameMustIsEmptyError)
    // 文字数チェック
    require(isLessThan(userName.value, User.userNameLength), UserNameLessThanError)
  }

  // パスワードのチェック
  {

    // 必須チェック
    require(isNonEmpty(password.value), PasswordMustIsEmptyError)
    // 文字数チェック
    require(isMoreThan(password.value, User.passwordLength), PasswordNameLessThanError)
  }

  /**
    * パスワードのマッチングを行う
    */
  def matchingPassword(plainPassword: PlainPassword, hashedPassword: HashedPassword): Boolean =
    (hashedPassword.value == createHashedPassword(password).value)

  /**
    * 平文パスワードをハッシュ化する
    * @param plainPassword 平文パスワード
    * @return ハッシュ化パスワードを持ったUserオブジェクト
    *         TODO ハッシュ化の正式実行
    */
  def createHashedPassword(plainPassword: PlainPassword): HashedPassword =
    HashedPassword("hashed" + plainPassword.value)

  /**
    * メールアドレスフォーマットチェック
    * Userに設定可能なメールアドセスの詳細を実装
    *   例）GMailのみ許可する、.co.jpドメインのみ許可するなど
    * @return
    */
  private def isValidMailAddress: Boolean = {
    val patternFormat = "[^@]+@[^@]+"
    val pattern       = Pattern.compile(patternFormat)
    pattern.matcher(mailAddress.value).matches()
  }
}
