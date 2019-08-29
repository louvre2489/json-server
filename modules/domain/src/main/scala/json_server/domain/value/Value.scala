package json_server.domain.value

trait Value[T] extends Any {
  def value: T
}
