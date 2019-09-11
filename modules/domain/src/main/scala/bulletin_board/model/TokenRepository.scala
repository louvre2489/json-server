package bulletin_board.model

import bulletin_board.domain.value.{ Token, UserId }

trait TokenRepository {

  /**
    * トークンを登録
    * @param token
    * @param userId
    * @return
    */
  def createToken(token: Token, userId: UserId): Token

  /**
    * トークンから紐づくUserIdを取得
    * @param token
    * @return
    */
  def findToken(token: Token): Option[UserId]

}
