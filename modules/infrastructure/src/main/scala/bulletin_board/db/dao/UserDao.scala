package bulletin_board.db.dao

import bulletin_board.common.{ DomainException, FatalException }
import bulletin_board.domain.User
import bulletin_board.domain.value.{ HashedPassword, MailAddress, MessageId, PlainPassword, UserId, UserName }
import bulletin_board.model.UserRepository
import org.slf4j.Logger

class UserDao()(implicit logger: Logger) extends BaseDao with UserRepository[User, UserId] {

  protected val schema = "user:"

  /**
    * ユーザー登録
    * @param entity
    * @param hashedPassword ハッシュ化済パスワード
    * @return
    */
  override def save(entity: User, hashedPassword: HashedPassword): Either[DomainException, User] = {

    // ユーザーIDの採番
    val userId: Long = IdFactory().createUserId()

    // ユーザー登録
    val entryUser = Map(UserFields.userName -> entity.userName.value,
                        UserFields.mailAddress -> entity.mailAddress.value,
                        UserFields.password    -> hashedPassword.value)

    logger.debug("登録内容：" + entryUser.toString())

    val result = run[Boolean](r => r.hmset(schema + userId, entryUser))

    if (result) {
      Right(entity.copy(userId = Some(UserId(userId))))
    } else {
      Left(DomainException(FatalException, MessageId("DB_ERROR")))
    }
  }

  /**
    * IDを元にユーザー情報を取得する
    * ただし、パスワードは漏洩対策として未設定の状態でユーザー情報を返す
    * @param id データのキーとなるID
    * @return
    */
  override def findById(id: UserId): Option[User] = {

    val user = run[Option[Map[String, String]]](
      r => r.hmget(schema + id.value, UserFields.mailAddress, UserFields.userName)
    )

    user.map[User](
      u => User(Some(id), MailAddress(u(UserFields.mailAddress)), UserName(u(UserFields.userName)), PlainPassword(""))
    )
  }

  /**
    * DBに登録されているパスワードを取得する
    * @param id
    * @return
    */
  override def getPassword(id: UserId): HashedPassword = {

    val user = run[Option[Map[String, String]]](
      r => r.hmget(schema + id.value, UserFields.password)
    )

    user.map[HashedPassword](u => HashedPassword(u(UserFields.password))).getOrElse(HashedPassword(""))
  }

  object UserFields {

    val userID: String = "USER_ID"

    val mailAddress: String = "MAIL_ADDRESS"

    val userName: String = "USER_NAME"

    val password: String = "PASSWORD"
  }
}
