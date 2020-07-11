import sbt.Keys._

object SbtAssembly {
  lazy val assemblyCommonSettings = Seq(
    version := "0.1-SNAPSHOT",
    scalaVersion := "2.12.8"
//    test in assembly : = {}
  )

}
