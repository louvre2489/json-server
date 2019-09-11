package bulletin_board.application.usecase

import bulletin_board.common.{ DomainException, ParameterException, RuleException }
import bulletin_board.domain.{ Post, Thread }
import bulletin_board.domain.value.{ MessageId, PostId, ThreadId, Token }
import bulletin_board.model.PostFindModel.PostFindResponse
import bulletin_board.model.ThreadFindModel.{ ThreadFindRequest, ThreadFindResponse }
import bulletin_board.model.{ PostRepository, ThreadRepository, TokenRepository }
import org.slf4j.Logger

trait ThreadFindUseCase {

  def findThread(
      token: Token,
      threadRequest: ThreadFindRequest
  )(implicit logger: Logger,
    threadRepository: ThreadRepository[Thread, ThreadId],
    postRepository: PostRepository[Post, PostId],
    tokenRepository: TokenRepository): Either[DomainException, ThreadFindResponse]
}

class ThreadFindUseCaseImpl extends ThreadFindUseCase {

  def findThread(
      token: Token,
      threadRequest: ThreadFindRequest
  )(implicit logger: Logger,
    threadRepository: ThreadRepository[Thread, ThreadId],
    postRepository: PostRepository[Post, PostId],
    tokenRepository: TokenRepository): Either[DomainException, ThreadFindResponse] = {

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

            val threadId = t.threadId.getOrElse(ThreadId(-1))

            // スレッド内のポストを取得する
            val posts = postRepository.findPost(threadId)

            Right(
              ThreadFindResponse(
                threadId.value,
                t.title.value,
                t.tags.values.map(_.tagName),
                "%tY/%<tm/%<td %<tH:%<tM:%<tS" format t.created_at.value,
                t.created_by.value,
                posts.map(p => {
                  PostFindResponse(p.postId.getOrElse(PostId(-1)).value,
                                   p.threadId.value,
                                   p.content.value,
                                   "%tY/%<tm/%<td %<tH:%<tM:%<tS" format p.created_at.value,
                                   p.created_by.value)
                })
              )
            )
          }
        }
      }
    }
  }
}
