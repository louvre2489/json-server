import sbt._
import sbt.Keys._
import org.scalafmt.sbt.ScalafmtPlugin.autoImport._

object Settings {

  val coreSettings = Seq(
    organization := "noriaki-tsushi",
    scalaVersion := "2.12.8",
    scalacOptions ++= {
      Seq(
        "-feature",
        "-deprecation",
        "-unchecked",
        "-encoding",
        "UTF-8",
        "-language:_"
      ) ++ {
        CrossVersion.partialVersion("2.12.8") match {
          case Some((2L, scalaMajor)) if scalaMajor == 12 =>
            Seq.empty
          case Some((2L, scalaMajor)) if scalaMajor <= 11 =>
            Seq(
              "-Yinline-warnings"
            )
        }
      }
    },
    scalafmtOnCompile in ThisBuild := true,
    libraryDependencies ++= Seq(
      Config.config,
      Logback.logback,
      ScalaTest.core % Test
    )
  )
}
