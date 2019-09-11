package bulletin_board.model.base

trait HasIDRepository[A, ID] extends BaseRepository[A] {

  /**
    * Find the item that designated by ID
    * @param id データのキーとなるID
    * @return
    */
  def findById(id: ID): Option[A]
}
