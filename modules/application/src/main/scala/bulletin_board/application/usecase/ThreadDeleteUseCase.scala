package bulletin_board.application.usecase

import bulletin_board.common.{ DomainException, ParameterException, RuleException }
import bulletin_board.domain.{ Tag, Tags, Thread }
import bulletin_board.domain.value.{ MessageId, ThreadId, Token }
import bulletin_board.model.ThreadDeleteModel.{ ThreadDeleteRequest, ThreadDeleteResponse }
import bulletin_board.model.{ ThreadRepository, TokenRepository }
import org.slf4j.Logger

trait ThreadDeleteUseCase {

  def deleteThread(
      token: Token,
      threadRequest: ThreadDeleteRequest
  )(implicit logger: Logger,
    threadRepository: ThreadRepository[Thread, ThreadId],
    tokenRepository: TokenRepository): Either[DomainException, ThreadDeleteResponse]
}

class ThreadDeleteUseCaseImpl extends ThreadDeleteUseCase {

  def deleteThread(
      token: Token,
      threadRequest: ThreadDeleteRequest
  )(implicit logger: Logger,
    threadRepository: ThreadRepository[Thread, ThreadId],
    tokenRepository: TokenRepository): Either[DomainException, ThreadDeleteResponse] = {

    // トークンチェック
    val userId = tokenRepository.findToken(token)

    // 存在しない場合はエラー
    userId match {
      case None => {
        // 無効なトークンによるアクセス
        Left(DomainException(ParameterException, MessageId("INVALID_TOKEN")))
      }
      case Some(u) => {
        // スレッドの情報を取得する
        val thread = threadRepository.findById(threadRequest.threadID)

        thread match {
          // 存在しない場合はエラー
          case None => Left(DomainException(RuleException, MessageId("INVALID_THREAD")))
          case Some(t) => {

            val createResult = t.delete(u)

            createResult match {
              case Left(e) => Left(e)
              case Right(thread) =>
                Right(
                  ThreadDeleteResponse(
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
  }
}
