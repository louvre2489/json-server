package bulletin_board.application.usecase

import java.util.Date

import bulletin_board.common.{ DomainException, ParameterException }
import bulletin_board.domain.{ Tag, Tags, Thread }
import bulletin_board.domain.value.{ MessageId, ThreadCreatedAt, ThreadId, Token }
import bulletin_board.model.ThreadCreateModel.{ ThreadCreateRequest, ThreadCreateResponse }
import bulletin_board.model.{ TagRepository, ThreadRepository, TokenRepository }
import org.slf4j.Logger

trait ThreadCreateUseCase {

  def createThread(
      token: Token,
      threadRequest: ThreadCreateRequest
  )(implicit logger: Logger,
    threadRepository: ThreadRepository[Thread, ThreadId],
    tagRepository: TagRepository[Tags],
    tokenRepository: TokenRepository): Either[DomainException, ThreadCreateResponse]
}

class ThreadCreateUseCaseImpl extends ThreadCreateUseCase {

  def createThread(
      token: Token,
      threadRequest: ThreadCreateRequest
  )(implicit logger: Logger,
    threadRepository: ThreadRepository[Thread, ThreadId],
    tagRepository: TagRepository[Tags],
    tokenRepository: TokenRepository): Either[DomainException, ThreadCreateResponse] = {

    // トークンチェック
    val userId = tokenRepository.findToken(token)

    // 存在しない場合はエラー
    userId match {
      case None => {
        // 無効なトークンによるアクセス
        Left(DomainException(ParameterException, MessageId("INVALID_TOKEN")))
      }
      case Some(u) => {
        val thread =
          Thread(None, threadRequest.title, Tags(None, threadRequest.tags.toSet), ThreadCreatedAt(new Date()), u, None)

        val createResult = thread.save

        createResult match {
          case Left(e) => Left(e)
          case Right(thread) =>
            Right(
              ThreadCreateResponse(
                thread.threadId.getOrElse(ThreadId(-1)).value,
                thread.title.value,
                thread.tags.values.map(_.tagName),
                "%tY/%<tm/%<td %<tH:%<tM:%<tS" format thread.created_at.value,
                thread.created_by.value
              )
            )
        }
      }
    }
  }
}
