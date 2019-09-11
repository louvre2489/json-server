package bulletin_board.model

import bulletin_board.domain.value.{ PostId, ThreadId }

object PostFindModel {
  case class PostFindRequest(threadId: ThreadId, postId: PostId)                                               {}
  case class PostFindResponse(id: Long, threadId: Long, content: String, created_at: String, created_by: Long) {}
}
