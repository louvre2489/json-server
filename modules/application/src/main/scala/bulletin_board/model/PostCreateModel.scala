package bulletin_board.model

import bulletin_board.domain.value.{ PostContent, ThreadId }

object PostCreateModel {
  case class PostCreateRequest(threadId: ThreadId, content: PostContent)                                         {}
  case class PostCreateResponse(id: Long, threadId: Long, content: String, created_at: String, created_by: Long) {}
}
