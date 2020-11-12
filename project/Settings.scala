import sbt.Keys._
import sbt._

object Settings {

  val circeVersion     = "0.12.3"

  val redisClientVersion = "3.20"

  val scalaTestVersion = "3.2.0"
  val scalacticVersion = "3.2.0"

  val kamonVersion = "2.1.4"

  val baseSettings = Seq(
    libraryDependencies ++= Seq(
        "ch.qos.logback"             % "logback-classic"     % "1.2.3",
        "com.typesafe.scala-logging" %% "scala-logging"      % "3.9.2",
        "io.kamon"                   %% "kamon-bundle"       % kamonVersion,
        "io.kamon"                   %% "kamon-datadog"      % kamonVersion
      )
  )

}
