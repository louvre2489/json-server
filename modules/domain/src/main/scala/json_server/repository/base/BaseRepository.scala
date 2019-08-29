package json_server.repository.base

trait BaseRepository[A, ID] {

  /**
    * Save the item
    * @param entity エンティティ
    * @return
    */
  def save(entity: A): Either[Exception, Unit]
}
