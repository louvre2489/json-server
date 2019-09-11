package bulletin_board.model

import bulletin_board.common.DomainException
import bulletin_board.domain.value.{ HashedPassword, UserId }
import bulletin_board.model.base.HasIDRepository

trait UserRepository[A, ID] extends HasIDRepository[A, ID] {

  /**
    * エンティティ単体での保存は不可
    * `save(User, HashedPassword)`を呼び出すこと
    * @param entity エンティティ
    * @return
    */
  @deprecated(message = "使用禁止", since = "")
  override def save(entity: A): Either[DomainException, A] = throw new IllegalCallerException()

  /**
    * ハッシュ化されたパスワードでユーザーを作成する
    * @param entity
    * @param hashedPassword ハッシュ化済パスワード
    * @return
    */
  def save(entity: A, hashedPassword: HashedPassword): Either[DomainException, A]

  /**
    * 指定したユーザーのパスワードを取得する
    * @param id
    * @return
    */
  def getPassword(id: UserId): HashedPassword
}
