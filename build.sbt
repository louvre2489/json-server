import Settings._

val baseName = "bulletin_board"

val prjDomain         = "domain"
val prjApplication    = "application"
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
      Akka.akka_slf4j,
      AkkaHttp.akka_http,
      AkkaHttp.akka_http_spray_json,
      AkkaHttp.akka_http_testkit % Test,
      Swagger.swagger,
      Rs.rs,
      Cors.cors,
      Circe.core,
      Circe.generic,
      Circe.parser,
      Redis.redis
    )
  )
  .settings(coreSettings)
  .settings(
    // Docker Settings
    packageName in Docker := "bulletin_board",
    version in Docker := "0.1.0",
    dockerBaseImage := "openjdk:8-jdk-alpine",
    dockerExposedPorts := List(5000),
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
