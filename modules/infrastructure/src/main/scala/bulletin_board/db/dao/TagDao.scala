package bulletin_board.db.dao

import bulletin_board.common.DomainException
import bulletin_board.domain.value.ThreadId
import bulletin_board.domain.{ Tag, Tags }
import bulletin_board.model.TagRepository
import org.slf4j.Logger

import scala.collection.mutable.{ Map => mMap }

class TagDao()(implicit logger: Logger) extends BaseDao with TagRepository[Tags] {

  protected val schema = "tags:"

  /**
    * タグの登録
    * @param entity エンティティ
    * @return
    */
  override def save(
      entity: Tags
  ): Either[DomainException, Tags] = {

    val tags = entity.values

    // IDはスレッドのIDと同じ
    val tagId = entity.threadId.getOrElse(ThreadId(-1)).value

    val entryTags = mMap.empty[String, String]

    // タグの件数だけ処理を行う
    tags.zipWithIndex.foreach {
      case (tag: Tag, i: Int) => {
        entryTags(TagFields.tag + i) = tag.tagName
      }
    }

    logger.debug("登録内容：" + entryTags.toString())

    val result = run[Boolean](r => r.hmset(schema + tagId, entryTags))

    Right(entity)
  }

  /**
    * タグの取得
    * スレッドIDを指定して、それに紐づくタグを取得する
    * @param threadId
    * @return
    */
  override def tags(threadId: ThreadId): Tags = {

    // IDはスレッドのIDと同じ
    val tagId = threadId.value

    // キーに登録されている値を全て取得する
    val tags = run[Option[List[String]]](r => r.hvals(schema + tagId))

    tags match {
      case None                         => Tags(Some(threadId), Set.empty)(this)
      case Some(list) if (list.isEmpty) => Tags(Some(threadId), Set.empty)(this)
      case Some(list) => {
        Tags(Some(threadId), list.toSet.map(t => Tag(t)))(this)
      }
    }
  }

  object TagFields {

    val tagID: String = "TAG_ID"

    val tag: String = "TAG_"
  }
}
