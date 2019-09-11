package bulletin_board.db.dao

import java.text.SimpleDateFormat
import java.util.Date

import bulletin_board.common.{ DomainException, FatalException }
import bulletin_board.domain.value.{ MessageId, PostContent, PostCreatedAt, PostId, ThreadId, UserId }
import bulletin_board.domain.{ Post, Thread }
import bulletin_board.model.{ PostRepository, ThreadRepository }
import org.slf4j.Logger

import scala.collection.Map

class PostDao()(implicit logger: Logger) extends BaseDao with PostRepository[Post, PostId] {

  protected val schema = "post:"

  private val dateTimeFormat: String = "yyyy/MM/dd HH:mm:ss"

  /**
    * ポストの登録
    * @param entity
    * @param threadRepository
    * @return
    */
  override def savePost(
      entity: Post
  )(implicit threadRepository: ThreadRepository[Thread, ThreadId]): Either[DomainException, Post] = {

    /**
      * 作成日時の抽出
      * @param post
      * @return
      */
    def createTime(post: Post): String =
      "%tY/%<tm/%<td %<tH:%<tM:%<tS" format post.created_at.value

    // ポストIDの採番
    val postId: Long = IdFactory().createPostId()

    // ポスト登録
    val entryPost = Map(
      PostFields.threadID  -> entity.threadId.value,
      PostFields.content   -> entity.content.value,
      PostFields.createdBy -> entity.created_by.value,
      PostFields.createdAt -> createTime(entity),
    )

    logger.debug("登録内容：" + entryPost.toString())

    val result = run[Boolean](r => r.hmset(schema + postId, entryPost))

    if (result) {
      Right(entity.copy(postId = Some(PostId(postId)))(threadRepository, this))
    } else {
      Left(DomainException(FatalException, MessageId("DB_ERROR")))
    }
  }

  /**
    * ポストの取得
    * @param threadId
    * @param postId
    * @param threadRepository
    * @return
    */
  override def findPost(threadId: ThreadId,
                        postId: PostId)(implicit threadRepository: ThreadRepository[Thread, ThreadId]): Option[Post] = {

    val post = run[Option[Map[String, String]]](
      r => r.hmget(schema + postId.value, PostFields.content, PostFields.createdBy, PostFields.createdAt)
    )

    post match {
      case None                   => None
      case Some(m) if (m.isEmpty) => None
      case Some(t) => {

        Some(
          Post(
            Some(postId),
            threadId,
            PostContent(t(PostFields.content)),
            stringToDate(t(PostFields.createdAt)),
            UserId(t(PostFields.createdBy).toLong)
          )(threadRepository, this)
        )
      }
    }
  }

  /**
    * 指定スレッド内のポストを全て取得する
    * @param threadId
    * @return
    */
  def findPost(threadId: ThreadId)(implicit threadRepository: ThreadRepository[Thread, ThreadId]): List[Post] = {

    // TODO スレッドに紐づくポストを全て返す
    // 固定でポストID：1のポストを返す
    this.findPost(threadId, PostId(1)).fold(Nil: List[Post])(p => List(p)) ++
    this.findPost(threadId, PostId(2)).fold(Nil: List[Post])(p => List(p))
  }

  private def stringToDate(targetDate: String): PostCreatedAt = {

    val formatter = new SimpleDateFormat(dateTimeFormat);
    PostCreatedAt(formatter.parse(targetDate))
  }

  object PostFields {

    val postID: String = "POST_ID"

    val threadID: String = "THREAD_ID"

    val content: String = "CONTENT"

    val createdBy: String = "CREATED_BY"

    val createdAt: String = "CREATED_AT"
  }
}
