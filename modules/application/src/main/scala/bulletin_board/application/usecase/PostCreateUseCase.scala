package bulletin_board.application.usecase

import java.util.Date

import bulletin_board.common.{ DomainException, ParameterException }
import bulletin_board.domain.{ Post, Thread }
import bulletin_board.domain.value.{ MessageId, PostCreatedAt, PostId, ThreadId, Token }
import bulletin_board.model.PostCreateModel.{ PostCreateRequest, PostCreateResponse }
import bulletin_board.model.{ PostRepository, ThreadRepository, TokenRepository }
import org.slf4j.Logger

trait PostCreateUseCase {

  def createPost(
      token: Token,
      postRequest: PostCreateRequest
  )(implicit logger: Logger,
    threadRepository: ThreadRepository[Thread, ThreadId],
    postRepository: PostRepository[Post, PostId],
    tokenRepository: TokenRepository): Either[DomainException, PostCreateResponse]
}

class PostCreateUseCaseImpl extends PostCreateUseCase {

  def createPost(
      token: Token,
      postRequest: PostCreateRequest
  )(implicit logger: Logger,
    threadRepository: ThreadRepository[Thread, ThreadId],
    postRepository: PostRepository[Post, PostId],
    tokenRepository: TokenRepository): Either[DomainException, PostCreateResponse] = {

    // トークンチェック
    val userId = tokenRepository.findToken(token)

    // 存在しない場合はエラー
    userId match {
      case None => {
        // 無効なトークンによるアクセス
        Left(DomainException(ParameterException, MessageId("INVALID_TOKEN")))
      }
      case Some(u) => {

        val post = Post(None, postRequest.threadId, postRequest.content, PostCreatedAt(new Date), u)

        // 登録
        val createResult = post.save

        createResult match {
          case Left(e) => Left(e)
          case Right(post) =>
            Right(
              PostCreateResponse(
                post.postId.getOrElse(PostId(-1)).value,
                post.threadId.value,
                post.content.value,
                "%tY/%<tm/%<td %<tH:%<tM:%<tS" format post.created_at.value,
                post.created_by.value
              )
            )

        }

      }
    }
  }
}
