import Settings._

val `domain` = (project in file("domain"))
  .settings(
    name := "domain"
  )

val `use-case` = (project in file("use-case"))
  .settings(
    name := "use-case"
  )
  .dependsOn(`domain`)

val `interface` = (project in file("interface"))
  .settings(
    name := "interface",
  )
  .dependsOn(
    `domain`
  )

val `infrastructure` = (project in file("infrastructure"))
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

val `application` = (project in file("application"))
  .settings(
    name := "application",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    )
  )
  .dependsOn(
    `domain`,
    `interface`,
    `infrastructure`,
  )

val `api-server` = (project in file("api-server"))
  .settings(
    name := "api-server",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http"   % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream" % akkaStreamVersion,
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      // https://mvnrepository.com/artifact/de.heikoseeberger/akka-http-circe
      "de.heikoseeberger" %% "akka-http-circe" % akkaCirceVersion,

    )
  )
  .dependsOn(
    `domain`,
    `use-case`,
    `interface`,
    `infrastructure`,
    `application`,
  )

val root = (project in file("."))
  .settings(
    name := "chatwork-mention-webhook"
  )
  .aggregate(
    `domain`,
    `use-case`,
    `infrastructure`,
    `api-server`,
  )
