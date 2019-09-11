package bulletin_board.db.dao

import java.text.SimpleDateFormat
import java.util.Date

import bulletin_board.common.{ DomainException, FatalException }
import bulletin_board.domain.{ Tag, Tags, Thread }
import bulletin_board.domain.value.{ MessageId, ThreadCreatedAt, ThreadId, ThreadLastPostAt, ThreadTitle, UserId }
import bulletin_board.model.{ TagRepository, ThreadRepository }
import org.slf4j.Logger

import scala.collection.Map

class ThreadDao()(implicit logger: Logger, tagRepository: TagRepository[Tags])
    extends BaseDao
    with ThreadRepository[Thread, ThreadId] {

  protected val schema = "thread:"

  private val dateTimeFormat: String = "yyyy/MM/dd HH:mm:ss"

  /**
    * スレッドの登録
    * @param entity エンティティ
    * @return
    */
  override def save(entity: Thread): Either[DomainException, Thread] = {

    /**
      * 作成日時の抽出
      * @param thread
      * @return
      */
    def createTime(thread: Thread): String =
      "%tY/%<tm/%<td %<tH:%<tM:%<tS" format thread.created_at.value

    // スレッドIDの採番
    val threadId: Long = IdFactory().createThreadId()

    // スレッド登録
    val entryThread = Map(
      ThreadFields.title      -> entity.title.value,
      ThreadFields.createdBy  -> entity.created_by.value,
      ThreadFields.createdAt  -> createTime(entity),
      ThreadFields.lastPostAt -> ""
    )

    logger.debug("登録内容：" + entryThread.toString())

    val result = run[Boolean](r => r.hmset(schema + threadId, entryThread))

    if (result) {
      Right(entity.copy(threadId = Some(ThreadId(threadId)))(this, tagRepository))
    } else {
      Left(DomainException(FatalException, MessageId("DB_ERROR")))
    }
  }

  /**
    * スレッドの取得
    * @param id データのキーとなるID
    * @return
    */
  override def findById(id: ThreadId): Option[Thread] = {

    /**
      * 最終更新日時を文字列から日付型に変換する
      * @param lastPostDate
      * @return
      */
    def lastPostAtStringToDate(lastPostDate: String): Option[ThreadLastPostAt] = {

      if (lastPostDate.isEmpty)
        None
      else
        Some(ThreadLastPostAt(stringToDate(lastPostDate)))
    }

    /**
      * 指定スレッドに紐づくタグの取得
      * @return
      */
    def tags: Tags = tagRepository.tags(id)

    val thread = run[Option[Map[String, String]]](
      r =>
        r.hmget(schema + id.value,
                ThreadFields.title,
                ThreadFields.createdBy,
                ThreadFields.createdAt,
                ThreadFields.lastPostAt)
    )

    thread match {
      case None                   => None
      case Some(m) if (m.isEmpty) => None
      case Some(t) => {
        // 最終投稿日
        val lastPostAt = t(ThreadFields.lastPostAt)

        Some(
          Thread(
            Some(id),
            ThreadTitle(t(ThreadFields.title)),
            tags,
            ThreadCreatedAt(stringToDate(t(ThreadFields.createdAt))),
            UserId(t(ThreadFields.createdBy).toLong),
            lastPostAtStringToDate(lastPostAt)
          )(this, tagRepository)
        )
      }
    }
  }

  /**
    * スレッドの削除
    * @param entity
    * @return
    */
  override def delete(entity: Thread): Either[DomainException, Thread] = {

    val threadId = entity.threadId.getOrElse(ThreadId(-1)).value

    val result = run[Option[Long]](r => r.del(schema + threadId))

    result match {
      case None    => Left(DomainException(FatalException, MessageId("DB_ERROR")))
      case Some(_) => Right(entity)
    }
  }

  /**
    * 文字列から日時への変換
    * @param targetDate
    * @return
    */
  private def stringToDate(targetDate: String): Date = {

    val formatter = new SimpleDateFormat(dateTimeFormat);
    formatter.parse(targetDate)
  }

  object ThreadFields {

    val threadID: String = "THREAD_ID"

    val title: String = "TITLE"

    val createdBy: String = "CREATED_BY"

    val createdAt: String = "CREATED_AT"

    val lastPostAt: String = "LAST_POST_AT"
  }
}
