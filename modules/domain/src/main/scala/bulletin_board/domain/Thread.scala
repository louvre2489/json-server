package bulletin_board.domain

import bulletin_board.common.{ DomainException, RuleException }
import bulletin_board.domain.entity.Entity
import bulletin_board.domain.value.{ MessageId, ThreadCreatedAt, ThreadId, ThreadLastPostAt, ThreadTitle, UserId }
import bulletin_board.model.{ TagRepository, ThreadRepository }

case class Thread(threadId: Option[ThreadId],
                  title: ThreadTitle,
                  tags: Tags,
                  created_at: ThreadCreatedAt,
                  created_by: UserId,
                  lastPost_at: Option[ThreadLastPostAt])(
    implicit threadRepository: ThreadRepository[Thread, ThreadId],
    tagRepository: TagRepository[Tags]
) extends Entity[Thread] {

  val titleLength = 40

  def save: Either[DomainException, Thread] = {

    /**
      * タグの登録
      * @param threadId
      */
    def enterTags(threadId: ThreadId): Unit = {
      // タグが設定されている場合は保存
      if (tags.nonEmpty)
        this.tags.copy(threadId = Some(threadId))(tagRepository).save
    }

    this.validate() match {
      case Some(e) => Left(e)
      case None => {

        val thread = threadRepository.save(this)

        thread match {
          case Left(e) => Left(e)
          case Right(t) => {

            // タグの登録
            enterTags(t.threadId.getOrElse(ThreadId(-1)))

            thread
          }
        }
      }
    }
  }

  def validate(): Option[DomainException] = {

    def titleValidation(): Option[DomainException] = {
      // タイトルは40文字以下
      if (isInputEmpty(title.value))
        Some(DomainException(RuleException, MessageId("MUST_INPUT"), Seq("タイトル")))
      else if (!isLessThan(title.value, titleLength))
        Some(DomainException(RuleException, MessageId("LENGTH_LESS_THAN_N"), Seq("タイトル", titleLength.toString)))
      else
        None
    }

    titleValidation() match {
      case Some(e) => Some(e)
      case None    => None
    }
  }

  /**
    *  ログインしているユーザーが作成したスレッドであれば削除を行う
    * 自分が作成したスレッドでなければエラーとする
    * @param loginUserId
    * @return
    */
  def delete(loginUserId: UserId): Either[DomainException, Thread] = {

    /**
      * スレッドは作成者のみが削除可能
      * @return true: 削除可能 false:削除不可
      */
    def canDelete(): Boolean = {
      loginUserId.value == created_by.value
    }

    if (canDelete())
      threadRepository.delete(this)
    else
      Left(DomainException(RuleException, MessageId("CANNOT_DELETE_THREAD")))
  }
}
