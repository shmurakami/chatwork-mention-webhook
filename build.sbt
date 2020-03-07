import Settings._

val `domain` = (project in file("domain"))
  .settings(
    name := "domain"
  )

val `api-server` = (project in file("api-server"))
  .settings(
    name := "api-server"
  )
  .dependsOn(`domain`)

val root = (project in file("."))
  .settings(
    name := "chatwork-mention-webhook"
  )
  .aggregate(
    `api-server`,
    `domain`,
  )
