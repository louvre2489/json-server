package bulletin_board.db.dao

import org.slf4j.Logger

object IdFactory {

  def apply()(implicit logger: Logger) = new IdFactory()
}

class IdFactory(implicit logger: Logger) extends BaseDao {

  /**
    * スキーマ指定なし
    */
  protected val schema = ""

  private val KEY_USER_ID: String   = "user_id"
  private val KEY_THREAD_ID: String = "thread_id"
  private val KEY_POST_ID: String   = "post_id"

  /**
    * ユーザーIDの採番
    * @return
    */
  def createUserId(): Long = run(r => r.incr(KEY_USER_ID)).getOrElse(-1)

  /**
    * スレッドIDの採番
    * @return
    */
  def createThreadId(): Long = run(r => r.incr(KEY_THREAD_ID)).getOrElse(-1)

  /**
    * ポストIDの採番
    * @return
    */
  def createPostId(): Long = run(r => r.incr(KEY_POST_ID)).getOrElse(-1)
}
