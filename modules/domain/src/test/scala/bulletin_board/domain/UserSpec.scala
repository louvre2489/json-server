package bulletin_board.domain

import bulletin_board.base.BaseSpec
import bulletin_board.common.{ DomainException,  RuleException }
import bulletin_board.domain.value.{ HashedPassword, MailAddress, PlainPassword, UserId, UserName }
import bulletin_board.model.UserRepository

/**
  * `bulletin_board.domain.USer`のテスト
  */
class UserSpec extends BaseSpec {

  /**
    * `USer`を生成するために必要なパラメーター
    */
  implicit val userRepository: UserRepository[User, UserId] = new UserRepository[User, UserId] {

    override def findById(id: UserId): Option[User] = ???

    override def getPassword(id: UserId): HashedPassword = ???

    override def save(entity: User, hashedPassword: HashedPassword): Either[DomainException, User] = ???
  }

  behavior of "User"

  "ユーザー名" should "未入力はエラー" in {

    val emptyUserNameUser =
      User(None, MailAddress("aaa@bb.cc"), UserName(""), PlainPassword("123456789012345"))

    emptyUserNameUser.validate() match {
      case None => fail()
      case Some(e) => {
        assert(e.exceptionType === RuleException)
        assert(e.message === "名前を入力してください。")
      }
    }
  }

  it should "10文字より大きい場合はエラー" in {

    val invalidUserNameUser =
      User(None, MailAddress("aaa@bb.cc"), UserName("12345678901"), PlainPassword("123456789012345"))

    invalidUserNameUser.validate() match {
      case None => fail()
      case Some(e) => {
        assert(e.exceptionType === RuleException)
        assert(e.message === "名前は10文字以下で入力してください。")
      }
    }
  }

  it should "10文字以内であればチェックOK" in {

    val invalidUserNameUser =
      User(None, MailAddress("aaa@bb.cc"), UserName("1234567890"), PlainPassword("123456789012345"))

    invalidUserNameUser.validate() match {
      case None    => succeed
      case Some(_) => fail()
    }
  }

}
