import Settings._
import AkkaSettings._
import DockerSettings._
import SbtAssembly._

val `domain` = (project in file("domain"))
  .settings(
    name := "domain"
  )

val `interface` = (project in file("interface"))
  .settings(baseSettings)
  .settings(
    name := "interface",
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
    ),
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
  .enablePlugins(AkkaGrpcPlugin)
  .settings(baseSettings)
  .settings(
    name := "modules",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-discovery" % akkaVersion,

      "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
      "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
      "org.scalactic" %% "scalactic" % scalacticVersion % "test",
    ),
    akkaGrpcGeneratedSources := Seq(AkkaGrpc.Server, AkkaGrpc.Client)
  )
  .dependsOn(
    `domain`,
    `interface`,
    `infrastructure`,
  )

val `api-client` = (project in file("application/api-client"))
  .settings(baseSettings)
  .settings(
    name := "api-client",
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      // https://mvnrepository.com/artifact/de.heikoseeberger/akka-http-circe
      "de.heikoseeberger" %% "akka-http-circe" % akkaCirceVersion,
    ),
  )
  .dependsOn(
    `domain`,
    `interface`,
    `infrastructure`,
    `modules`,
  )

val `api-server` = (project in file("application/api-server"))
  .enablePlugins(DockerPlugin, JavaAppPackaging)
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
    ),
    // docker
    dockerBaseImage := baseImage,
    packageName in Docker := "api-server",
    version in Docker := applicationVersion,
    // how to set dynamically?
    dockerExposedPorts := Seq(8080, 18080),
    dockerRepository := dockerImageRepositoryURI,
    // java options
    javaOptions in Universal ++= Seq(
      "-server"
    )
  )
  .dependsOn(
    `domain`,
    `interface`,
    `infrastructure`,
    `modules`,
  )

val `read-model-updater` = (project in file("application/read-model-updater"))
  .enablePlugins(DockerPlugin, JavaAppPackaging)
  .settings(baseSettings)
  .settings(assemblyCommonSettings)
  .settings(
    name := "read-model-updater",
    mainClass in assembly := Some("com.shmrkm.chatworkWebhook.readModelUpdater.Main"),
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-stream" % AkkaSettings.akkaStreamVersion,
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
    ),
    // docker
    dockerBaseImage := baseImage,
    packageName in Docker := "read-model-updater",
    dockerRepository := dockerImageRepositoryURI,
    version in Docker := applicationVersion
  )
  .dependsOn(
    `domain`,
    `interface`,
    `infrastructure`,
    `modules`,
  )

val root = (project in file("."))
  .settings(
    name := "chatwork-mention-webhook",
  )
  .aggregate(
    `domain`,
    `infrastructure`,
    `api-server`,
    `read-model-updater`,
  )
