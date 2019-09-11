package bulletin_board.model

import bulletin_board.common.DomainException
import bulletin_board.domain.Thread
import bulletin_board.domain.value.ThreadId
import bulletin_board.model.base.HasIDRepository

trait PostRepository[A, ID] extends HasIDRepository[A, ID] {

  /**
    * 使用禁止
    * @param entity エンティティ
    * @return
    */
  @deprecated(message = "使用禁止", since = "")
  override def save(entity: A): Either[DomainException, A] = throw new IllegalCallerException()

  /**
    * `ThreadRepository`が必要になるので登録時はこちらを使用する
    * @param entity
    * @param threadRepository
    * @return
    */
  def savePost(entity: A)(
      implicit threadRepository: ThreadRepository[Thread, ThreadId]
  ): Either[DomainException, A]

  /**
    * 使用禁止
    * @param id データのキーとなるID
    * @return
    */
  override def findById(id: ID): Option[A] = throw new IllegalCallerException()

  def findPost(threadId: ThreadId, PostId: ID)(implicit threadRepository: ThreadRepository[Thread, ThreadId]): Option[A]

  def findPost(threadId: ThreadId)(implicit threadRepository: ThreadRepository[Thread, ThreadId]): List[A]

}
