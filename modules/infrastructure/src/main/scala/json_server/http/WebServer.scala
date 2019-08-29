package json_server.http

import java.io.InputStream
import java.security.{ KeyStore, SecureRandom }

import akka.actor.ActorSystem
import akka.http.scaladsl.{ ConnectionContext, Http, HttpsConnectionContext }
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import javax.net.ssl.{ KeyManagerFactory, SSLContext, TrustManagerFactory }
import scalikejdbc.{ DB, SQL }

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.io.StdIn
import scalikejdbc.config._

import scala.util.{ Failure, Success }

object WebServer {

  val conf = ConfigFactory.load

  def main(args: Array[String]) {

    // DB セットアップ
//    DBs.setup()
//
//    // テストクエリ
//    DB readOnly { implicit session =>
//      val one = SQL("SELECT 1 AS one").map(rs => rs.int("one")).single.apply()
//      println(one)
//    }

    implicit val system: ActorSystem             = ActorSystem(conf.getString(ACTOR_SYSTEM_NAME))
    implicit val materializer: ActorMaterializer = ActorMaterializer()

    // needed for the future flatMap/onComplete in the end
    implicit val executionContext: ExecutionContext = system.dispatcher
    implicit val timeout: Timeout                   = Timeout(5.seconds)

    // Configuration of Server
    val host = conf.getString(MY_HOST)
    val port = conf.getInt(MY_PORT)

    val bindingFuture =
      Http().bindAndHandle(Routes().routes, host, port, connectionContext = createHttpsConnectionContext())

    bindingFuture.onComplete {
      case Success(_) =>
        system.log.info(s"Server online at http://$host:$port/")
      case Failure(ex) =>
        system.log.error(ex, "error!!!")
    }

    sys.addShutdownHook {
      bindingFuture
        .flatMap(_.unbind())
        .onComplete { _ =>
          materializer.shutdown()
          system.terminate()
        }
    }
  }

  private def createHttpsConnectionContext(): HttpsConnectionContext = {

    val password
      : Array[Char] = conf.getString(KEY_STORE_PASS).toCharArray // do not store passwords in code, read them from somewhere safe!

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
