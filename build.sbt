import Settings._

val baseName = "json-server"

val prjDomain = "domain"
val prjApplication = "application"
val prjInfrastructure = "infrastructure"

lazy val domain = (project in file(s"modules/$prjDomain"))
  .settings(
    name := s"$baseName-$prjDomain"
  )
  .settings(coreSettings)

lazy val application = (project in file(s"modules/$prjApplication"))
  .settings(
    name := s"$baseName-$prjApplication"
  )
  .settings(coreSettings)
  .dependsOn(domain)

lazy val infrastructure = (project in file(s"modules/$prjInfrastructure"))
  .settings(
    name := s"$baseName-$prjInfrastructure",
    libraryDependencies ++= Seq(
      Akka.akka_actor,
      Akka.akka_stream,
      Akka.akka_slf4j,
      Akka.akka_testkit % Test,
      AkkaHttp.akka_http,
      AkkaHttp.akka_http_spray_json,
      AkkaHttp.akka_http_testkit % Test,
      Swagger.swagger,
      Rs.rs,
      Cors.cors,
      Circe.core,
      Circe.generic,
      Circe.parser,
//      Spray.spray,
      ScalikeJdbc.scalikeJdbc,
      ScalikeJdbc.scalikeJdbcConfig,
      ScalikeJdbc.scalikeJdbcTest,
      H2.h2
    )
  )
  .settings(coreSettings)
  .settings(
    // Docker Settings
    packageName in Docker := "json-server",
    version in Docker := "0.1.0",
    dockerBaseImage := "openjdk:8u131-jdk-alpine",
    dockerExposedPorts := List(5000),
    dockerExposedVolumes := Seq("/opt/docker/logs"),
    dockerCmd := Nil
  )
  .dependsOn(domain, application)
  .enablePlugins(JavaServerAppPackaging, AshScriptPlugin, DockerPlugin)

lazy val root = (project in file("."))
  .settings(
    name := baseName,
    mainClass in (Compile, run) := Some("json_server.http.WebServer")
  )
  .aggregate(
    domain,
    application,
    infrastructure
  )
  .enablePlugins(JavaServerAppPackaging, AshScriptPlugin, DockerPlugin)

// .enablePlugins(ScalikejdbcPlugin)
