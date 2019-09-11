package bulletin_board.model

import bulletin_board.common.DomainException
import bulletin_board.model.base.HasIDRepository

trait ThreadRepository[A, ID] extends HasIDRepository[A, ID] {

  /**
    *
    * @param entity エンティティ
    * @return
    */
  override def save(entity: A): Either[DomainException, A]

  def delete(entity: A): Either[DomainException, A]
}
