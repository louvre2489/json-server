package json_server.repository.base

trait ListItemRepository[A, ID] extends BaseRepository[A, ID] {

  /**
    *  Get all items
    * @return
    */
  def findAll: List[A]

}
