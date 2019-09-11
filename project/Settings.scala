import sbt._
import sbt.Keys._
import org.scalafmt.sbt.ScalafmtPlugin.autoImport._


object Settings {

  val coreSettings = Seq(
    organization := "noriaki-tsushi",
    scalaVersion := "2.12.8",
//    scalaVersion := "2.13.0",
    scalacOptions ++= {
      Seq(
        "-feature",
        "-deprecation",
        "-unchecked",
        "-encoding",
        "UTF-8",
        "-language:_"
      )
    },
    scalafmtOnCompile in ThisBuild := true,
    libraryDependencies ++= Seq(
      Config.config,
      Logback.logback,
      ScalaTest.core % Test
    )
  )
}
