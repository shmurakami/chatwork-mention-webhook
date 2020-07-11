import Settings._
import SbtAssembly._

val `domain` = (project in file("domain"))
  .settings(
    name := "domain"
  )

val `interface` = (project in file("interface"))
  .settings(baseSettings)
  .settings(
    name := "interface",
  )
  .dependsOn(
    `domain`
  )

val `infrastructure` = (project in file("infrastructure"))
  .settings(baseSettings)
  .settings(
    name := "infrastructure",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-http"  % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream" % akkaStreamVersion,
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      "de.heikoseeberger" %% "akka-http-circe" % akkaCirceVersion,
      "net.debasishg" %% "redisclient" % redisClientVersion,
    )
  )
  .dependsOn(
    `domain`,
    `interface`,
  )

val `modules` = (project in file("modules"))
  .settings(baseSettings)
  .settings(
    name := "modules",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,

      "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
      "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
      "org.scalactic" %% "scalactic" % scalacticVersion % "test",
    )
  )
  .dependsOn(
    `domain`,
    `interface`,
    `infrastructure`,
  )

val `api-server` = (project in file("application/api-server"))
  .settings(baseSettings)
  .settings(assemblyCommonSettings)
  .settings(
    name := "api-server",
    mainClass in assembly := Some("com.shmrkm.chatworkWebhook.mention.Main"),
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http"   % akkaHttpVersion,
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      // https://mvnrepository.com/artifact/de.heikoseeberger/akka-http-circe
      "de.heikoseeberger" %% "akka-http-circe" % akkaCirceVersion,
    )
  )
  .dependsOn(
    `domain`,
    `interface`,
    `infrastructure`,
    `modules`,
  )

val `read-model-updater` = (project in file("application/read-model-updater"))
  .settings(baseSettings)
  .settings(assemblyCommonSettings)
  .settings(
    name := "read-model-updater",
    mainClass in assembly := Some("com.shmrkm.chatworkWebhook.readModelUpdater.Main"),
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-stream" % akkaStreamVersion,
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
    )
  )
  .dependsOn(
    `domain`,
    `interface`,
    `infrastructure`,
    `modules`,
  )

val root = (project in file("."))
  .settings(
    name := "chatwork-mention-webhook"
  )
  .aggregate(
    `domain`,
    `infrastructure`,
    `api-server`,
    `read-model-updater`,
  )
