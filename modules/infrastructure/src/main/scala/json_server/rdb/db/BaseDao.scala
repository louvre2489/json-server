package json_server.rdb.db

import scalikejdbc.DB

trait BaseDaoObject {

  def apply()(implicit db: DB): BaseDao
}

abstract class BaseDao()(implicit db: DB) {}
