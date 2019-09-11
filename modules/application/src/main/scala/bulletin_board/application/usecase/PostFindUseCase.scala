package bulletin_board.application.usecase

import bulletin_board.common.{ DomainException, ParameterException, RuleException }
import bulletin_board.domain.{ Post, Thread }
import bulletin_board.domain.value.{ MessageId, PostId, ThreadId, Token }
import bulletin_board.model.PostFindModel.{ PostFindRequest, PostFindResponse }
import bulletin_board.model.{ PostRepository, ThreadRepository, TokenRepository }
import org.slf4j.Logger

trait PostFindUseCase {

  def findPost(
      token: Token,
      postRequest: PostFindRequest
  )(implicit logger: Logger,
    threadRepository: ThreadRepository[Thread, ThreadId],
    postRepository: PostRepository[Post, PostId],
    tokenRepository: TokenRepository): Either[DomainException, PostFindResponse]
}

class PostFindUseCaseImpl extends PostFindUseCase {

  def findPost(
      token: Token,
      postRequest: PostFindRequest
  )(implicit logger: Logger,
    threadRepository: ThreadRepository[Thread, ThreadId],
    postRepository: PostRepository[Post, PostId],
    tokenRepository: TokenRepository): Either[DomainException, PostFindResponse] = {

    // トークンチェック
    val userId = tokenRepository.findToken(token)

    // 存在しない場合はエラー
    userId match {
      case None => {
        // 無効なトークンによるアクセス
        Left(DomainException(ParameterException, MessageId("INVALID_TOKEN")))
      }
      case Some(u) => {
        // ポストの情報を取得する
        val post = postRepository.findPost(postRequest.threadId, postRequest.postId)

        post match {
          // 存在しない場合はエラー
          case None => Left(DomainException(RuleException, MessageId("INVALID_POST")))
          case Some(p) => {
            Right(
              PostFindResponse(p.postId.getOrElse(PostId(-1)).value,
                               p.threadId.value,
                               p.content.value,
                               "%tY/%<tm/%<td %<tH:%<tM:%<tS" format p.created_at.value,
                               p.created_by.value)
            )
          }
        }
      }
    }
  }
}
