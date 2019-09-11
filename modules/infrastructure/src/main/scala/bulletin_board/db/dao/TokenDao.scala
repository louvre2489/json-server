package bulletin_board.db.dao

import bulletin_board.domain.value.{ Token, UserId }
import bulletin_board.model.TokenRepository
import com.typesafe.config.ConfigFactory
import org.slf4j.Logger

class TokenDao()(implicit logger: Logger) extends BaseDao with TokenRepository {

  val conf = ConfigFactory.load

  /**
    * 操作対象データのスキーマ
    */
  override protected val schema: String = "token:"

  // 有効期限
  private val expireTime = conf.getInt("token.expireTime")

  /**
    * アクセストークンの登録
    * @param token
    * @param userId
    * @return
    */
  override def createToken(token: Token, userId: UserId): Token = {

    logger.debug("登録内容 => " + s"token:${token.value} userId:${userId.value}")

    // トークン登録
    run[Boolean](r => r.set(schema + token.value, userId.value))

    // 有効期限設定
    run[Boolean](r => r.expire(schema + token.value, expireTime))

    token
  }

  /**
    * トークン取得
    * トークンが存在する場合は、有効期限の延長も同時に行う
    * @param token
    * @return
    */
  override def findToken(token: Token): Option[UserId] = {

    // トークン取得
    val userId = run[Option[String]](r => r.get(schema + token.value))

    userId match {
      case None => None
      case Some(u) => {

        // 取得に成功した場合は、有効期限の再設定を行う
        this.refreshToken(token)

        Some(UserId(u.toLong))
      }
    }
  }

  /**
    * アクセストークンの有効期限を再設定
    * @param token
    */
  protected def refreshToken(token: Token): Unit = {
    // 有効期限再設定
    run[Boolean](r => r.expire(schema + token.value, expireTime))
  }

}
