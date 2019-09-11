package bulletin_board.common

/**
  * 業務エラータイプ
  */
sealed trait DomainError {

  override def toString: String = errorMessage

  def errorMessage: String
}

/**
  * メールアドレス：必須
  */
object MailAddressIsEmptyError extends DomainError {
  override def errorMessage: String = "MAIL_ADDRESS_IS_EMPTY_ERROR"
}

/**
  * メールフォーマット
  */
object MailAddressFormatError extends DomainError {
  override def errorMessage: String = "MAIL_ADDRESS_FORMAT_ERROR"
}

/**
  * ユーザー名必須
  */
object UserNameMustIsEmptyError extends DomainError {
  override def errorMessage: String = "USER_NAME_IS_EMPTY_ERROR"
}

/**
  * ユーザー名文字数
  */
object UserNameLessThanError extends DomainError {
  override def errorMessage: String = "USER_NAME_MUST_INPUT_ERROR"
}

/**
  * パスワード必須
  */
object PasswordMustIsEmptyError extends DomainError {
  override def errorMessage: String = "PASSWORD_IS_EMPTY_ERROR"
}

/**
  * パスワード文字数
  */
object PasswordNameLessThanError extends DomainError {
  override def errorMessage: String = "PASSWORD_MUST_INPUT_ERROR"
}
