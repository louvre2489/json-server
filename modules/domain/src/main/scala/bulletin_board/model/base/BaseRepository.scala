package bulletin_board.model.base

import bulletin_board.common.DomainException

trait BaseRepository[A] {

  /**
    * 保存用インタフェース
    * @param entity エンティティ
    * @return
    */
  def save(entity: A): Either[DomainException, A]
}
