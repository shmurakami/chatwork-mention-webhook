import sbt.Keys._
import sbt._

object Settings {
  val akkaVersion = "2.6.3"

  val akkaHttpVersion = "10.1.11"
  val akkaStreamVersion = "2.5.26"

  val circeVersion = "0.12.3"
  val akkaCirceVersion = "1.31.0"

  val redisClientVersion = "3.20"

  val scalaTestVersion = "3.2.0"
  val scalacticVersion = "3.2.0"

  val baseSettings = Seq(
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"
    )
  )

}
