package bulletin_board.model

import bulletin_board.domain.value.ThreadId

object ThreadFindModel {
  case class ThreadFindRequest(threadID: ThreadId) {}
  case class ThreadFindResponse(id: Long,
                                title: String,
                                tags: Set[String] = Set(),
                                created_at: String,
                                created_by: Long,
                                posts: List[PostFindModel.PostFindResponse] = Nil) {}
}
