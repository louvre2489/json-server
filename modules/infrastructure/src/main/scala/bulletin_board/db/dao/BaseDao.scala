package bulletin_board.db.dao

import com.redis._
import org.slf4j.Logger

abstract class BaseDao()(implicit logger: Logger) {

  /**
    * 操作対象データのスキーマ
    */
  protected val schema: String

  /**
    * 接続情報
    */
  // TODO 設定情報はapplication.confに移動させる
  val clients = new RedisClientPool("localhost", 6379)

  /**
    * Redisコマンドの実行
    * @param f
    * @tparam A
    * @return Redisコマンドの実行結果
    */
  def run[A](f: RedisClient => A): A = {
    clients.withClient { client =>
      f(client)
    }
  }
}
