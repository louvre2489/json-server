package bulletin_board.model

import bulletin_board.domain.value.ThreadId

object ThreadDeleteModel {
  case class ThreadDeleteRequest(threadID: ThreadId) {}
  case class ThreadDeleteResponse(id: Long,
                                  title: String,
                                  tags: Set[String] = Set(),
                                  created_at: String,
                                  created_by: Long) {}
}
