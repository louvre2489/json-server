package json_server.domain.entity

trait Entity[ID] {

  def save: Either[Exception, Unit]
}
