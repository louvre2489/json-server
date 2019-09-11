package bulletin_board.domain

import bulletin_board.common.{ DomainException, RuleException }
import bulletin_board.domain.entity.Entity
import bulletin_board.domain.value.{ MessageId, PostContent, PostCreatedAt, PostId, ThreadId, UserId }
import bulletin_board.model.{ PostRepository, ThreadRepository }

case class Post(postId: Option[PostId],
                threadId: ThreadId,
                content: PostContent,
                created_at: PostCreatedAt,
                created_by: UserId)(
    implicit threadRepository: ThreadRepository[Thread, ThreadId],
    postRepository: PostRepository[Post, PostId]
) extends Entity[Post] {

  def save: Either[DomainException, Post] = {
    this.validate() match {
      case Some(e) => Left(e)
      case None => {

        // スレッドの存在チェック
        val thread = threadRepository.findById(this.threadId)

        thread match {
          // 存在しない場合はエラー
          case None => Left(DomainException(RuleException, MessageId("INVALID_THREAD")))
          case Some(t) => {

            //  登録
            postRepository.savePost(this)

            // TODO スレッドの最終更新日時を更新

          }
        }
      }
    }
  }

  def validate(): Option[DomainException] = {

    def contentValidation(): Option[DomainException] = {
      // タイトルは40文字以下
      if (isInputEmpty(content.value))
        Some(DomainException(RuleException, MessageId("MUST_INPUT"), Seq("ポスト")))
      else
        None
    }

    contentValidation() match {
      case Some(e) => Some(e)
      case None    => None
    }
  }
}
