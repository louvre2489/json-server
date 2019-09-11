package bulletin_board.http

import java.io.InputStream
import java.security.{ KeyStore, SecureRandom }

import akka.actor.ActorSystem
import akka.http.scaladsl.{ ConnectionContext, Http, HttpsConnectionContext }
import akka.stream.ActorMaterializer
import akka.util.Timeout
import bulletin_board.util.BulletinBoardLogFactory
import com.typesafe.config.ConfigFactory
import javax.net.ssl.{ KeyManagerFactory, SSLContext, TrustManagerFactory }

import scala.concurrent.{ Await, ExecutionContext }
import scala.concurrent.duration._

import scala.util.{ Failure, Success }

object WebServer {

  val conf = ConfigFactory.load

  def main(args: Array[String]) {

    implicit val system: ActorSystem                = ActorSystem(conf.getString(ACTOR_SYSTEM_NAME))
    implicit val materializer: ActorMaterializer    = ActorMaterializer()
    implicit val executionContext: ExecutionContext = system.dispatcher
    implicit val timeout: Timeout                   = Timeout(5.seconds)

    // Webサーバー設定
    val host         = conf.getString(MY_HOST)
    val port         = conf.getInt(MY_PORT)
    val enablesHttps = conf.getBoolean(ENABLE_HTTPS)

    val bindingFuture =
      if (enablesHttps)
        Http().bindAndHandle(Routes().routes, host, port, connectionContext = createHttpsConnectionContext())
      else
        Http().bindAndHandle(Routes().routes, host, port)

    bindingFuture.onComplete {
      case Success(_) =>
        BulletinBoardLogFactory.logger.info(s"Server online at https://$host:$port/")
      case Failure(ex) =>
        BulletinBoardLogFactory.logger.error(s"Error!! Not Started!! case:${ex}")
    }

    sys.addShutdownHook {
      bindingFuture
        .flatMap(_.unbind())
        .onComplete { _ =>
//          materializer.shutdown()

          system.terminate()
        }
    }

    Await.ready(bindingFuture, Duration.Inf)
  }

  /**
    *  Https接続用Context
    */
  private def createHttpsConnectionContext(): HttpsConnectionContext = {

    // TODO コード上に書くべきではない。本来どこで管理するべき？
    val password: Array[Char] = conf.getString(KEY_STORE_PASS).toCharArray

    val ks: KeyStore          = KeyStore.getInstance("PKCS12")
    val keystore: InputStream = getClass.getClassLoader.getResourceAsStream(conf.getString(KEY_STORE))

    require(keystore != null, "Keystore required!")

    ks.load(keystore, password)

    val keyManagerFactory: KeyManagerFactory = KeyManagerFactory.getInstance("SunX509")
    keyManagerFactory.init(ks, password)

    val tmf: TrustManagerFactory = TrustManagerFactory.getInstance("SunX509")
    tmf.init(ks)

    val sslContext: SSLContext = SSLContext.getInstance("TLS")
    sslContext.init(keyManagerFactory.getKeyManagers, tmf.getTrustManagers, new SecureRandom)

    ConnectionContext.https(sslContext)
  }
}
