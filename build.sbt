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

val `infrastructure` = (project in file("infrastructure"))
  .settings(
    name := "infrastructure"
  )

val `api-server` = (project in file("api-server"))
  .settings(
    name := "api-server",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http"   % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream" % akkaStreamVersion,
    )
  )
  .dependsOn(
    `domain`,
    `use-case`,
    `infrastructure`,
  )

val root = (project in file("."))
  .settings(
    name := "chatwork-mention-webhook"
  )
  .aggregate(
    `api-server`,
    `use-case`,
    `domain`,
  )
