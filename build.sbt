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
    `use-case`,
    `domain`,
  )
