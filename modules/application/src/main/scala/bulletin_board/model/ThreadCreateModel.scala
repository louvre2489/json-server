package bulletin_board.model

import bulletin_board.domain.value.ThreadTitle
import bulletin_board.domain.Tag

object ThreadCreateModel {
  case class ThreadCreateRequest(title: ThreadTitle, tags: Array[Tag]) {}
  case class ThreadCreateResponse(id: Long,
                                  title: String,
                                  tags: Set[String] = Set(),
                                  created_at: String,
                                  created_by: Long) {}
}
