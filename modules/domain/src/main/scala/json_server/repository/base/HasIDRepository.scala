package json_server.repository.base

trait HasIDRepository[A, ID] {

  /**
    * Find the item that designated by ID
    * @param id データのキーとなるID
    * @return
    */
  def findById(id: ID): Option[A]
}
