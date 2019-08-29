package json_server.rdb.db

import com.typesafe.config.ConfigFactory

object DBSettings {

  private val conf = ConfigFactory.load

  val poolName: Symbol = Symbol.apply(conf.getString(DB_POOL_FACTORY_NAME))

  private val url      = conf.getString(DB_URL)
  private val username = conf.getString(DB_USER)
  private val password = conf.getString(DB_PASS)

//  private val settings = ConnectionPoolSettings(
//    initialSize = conf.getInt(DB_POOL_INIT_SIZE),
//    maxSize = conf.getInt(DB_POOL_MAX_SIZE),
//    connectionTimeoutMillis = conf.getLong(DB_CONN_TIMEOUT_MILLIS),
//    validationQuery = conf.getString(DB_POOL_VALID_QUERY)
//  )
//
//  // initialize JDBC driver & connection pool
//  Class.forName(conf.getString(DB_DRIVER))
//
//  // after loading JDBC drivers
//  ConnectionPool.singleton(url, username, password)
//  ConnectionPool.add(poolName, url, username, password)
//
//  // all the connections are released, old connection pool will be abandoned
//  ConnectionPool.add(poolName, url, username, password, settings)
}
