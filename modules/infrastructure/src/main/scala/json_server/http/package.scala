package json_server

package object http {

  /**
    * サーバー証明証キーストア
    */
  val KEY_STORE = "https.keystore"

  /**
    * サーバー証明書パスワード
    */
  val KEY_STORE_PASS = "https.password"

  /**
    *  ActorSystem Name
    */
  val ACTOR_SYSTEM_NAME = "actor.system.name"

  /**
    * Configuration Parameter:host
    */
  val MY_HOST = "http.host"

  /**
    * Configuration Parameter:port
    */
  val MY_PORT = "http.port"

  /**
    * JWTアクセストークン
    */
  val ACCESS_TOKEN = "jwt.access-token"

  /**
    * リソースファイルパス
    */
  val RESOURCE_ROOT_PATH = "frontend"

  /**
    * フロントエンドルート
    */
  val FRONTEND_ROOT_PATH = "public"

  /**
    * HTMLのパス
    */
  val HTML_FILE_PATH = RESOURCE_ROOT_PATH + "/" + FRONTEND_ROOT_PATH + "/html/"
}
