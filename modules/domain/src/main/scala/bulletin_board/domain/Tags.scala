package bulletin_board.domain

import bulletin_board.common.DomainException
import bulletin_board.domain.entity.Entity
import bulletin_board.domain.value.ThreadId
import bulletin_board.model.TagRepository

case class Tag(tagName: String)

case class Tags(threadId: Option[ThreadId], private val tags: Set[Tag])(implicit tagRepository: TagRepository[Tags])
    extends Entity[Tags] {

  /**
    * 空のコレクションオブジェクトを生成する
    */
  def this(id: Option[ThreadId])(implicit tagRepo: TagRepository[Tags]) = this(id, Set.empty)(tagRepo)

  /**
    * このクラスのオブジェクトが保持しているTagの一覧
    * @return tags
    */
  def values: Set[Tag] = tags

  /**
    * タグの追加
    * パラメーターの機能を追加した新しいコレクションオブジェクトを生成して返す
    * @param tag 追加するタグ
    * @return 新しいコレクションオブジェクト
    */
  def addTag(tag: Tag): Tags = Tags(threadId, values + tag)

  /**
    * タグの設定有無を確認する
    * @return タグが設定されている場合にtrue
    */
  def nonEmpty: Boolean = values.nonEmpty

  /**
    * Tagsを全て保存する
    * @return
    */
  def save: Either[DomainException, Tags] = tagRepository.save(this)

  /**
    * バリデーションなし
    * @return
    */
  def validate(): Option[DomainException] = None
}
