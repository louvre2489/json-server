import sbt._

object Config {
  val version = "1.3.4"
  val config  = "com.typesafe" % "config" % version
}

object ScalaTest {
  val version        = "3.0.8"
  val core: ModuleID = "org.scalatest" %% "scalatest" % version
}

object Akka {
  val version      = "2.5.25"
  val akka_actor   = "com.typesafe.akka" %% "akka-actor" % version exclude ("com.typesafe", "config")
  val akka_slf4j   = "com.typesafe.akka" %% "akka-slf4j" % version
  val akka_stream  = "com.typesafe.akka" %% "akka-stream" % version
  val akka_testkit = "com.typesafe.akka" %% "akka-testkit" % version
}

object AkkaHttp {
  val version              = "10.1.9"
  val akka_http            = "com.typesafe.akka" %% "akka-http" % version
  val akka_http_spray_json = "com.typesafe.akka" %% "akka-http-spray-json" % version
  val akka_http_testkit    = "com.typesafe.akka" %% "akka-http-testkit" % version
}

object Swagger {
  val version = "2.0.3"
  val swagger = "com.github.swagger-akka-http" %% "swagger-akka-http" % version
}

object Rs {
  val version = "2.0.1"
  val rs      = "javax.ws.rs" % "javax.ws.rs-api" % version
}

object Cors {
  val version = "0.4.1"
  val cors    = "ch.megard" %% "akka-http-cors" % version
}

object Circe {
  val version = "0.11.1"
  val core    = "io.circe" %% "circe-core" % version
  val generic = "io.circe" %% "circe-generic" % version
  val parser  = "io.circe" %% "circe-parser" % version

  object Validation {
    val version = "0.0.6"
    val core    = "io.tabmo" %% "circe-validation-core" % version
  }
}

object Redis {
  val version = "3.10"
  val redis   = "net.debasishg" %% "redisclient" % version
}

object Logback {
  val version = "1.2.3"
  val logback = "ch.qos.logback" % "logback-classic" % version
}
