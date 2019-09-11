package bulletin_board.model

import bulletin_board.common.DomainException
import bulletin_board.domain.Tags
import bulletin_board.domain.value.ThreadId
import bulletin_board.model.base.BaseRepository

trait TagRepository[A] extends BaseRepository[A] {

  /**
    *
    * @param entity エンティティ
    * @return
    */
  override def save(entity: A): Either[DomainException, A]

  def tags(threadId: ThreadId): Tags

}
