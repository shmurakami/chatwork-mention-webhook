package com.shmrkm.chatworkWebhook.mention

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.shmrkm.chatworkWebhook.mention.controller.WebhookController
import com.typesafe.config.Config

import scala.io.StdIn

object Main {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("webhook")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val routes = new Routes(
      new WebhookController
    )

    val config: Config = system.settings.config.getConfig("chatwork-mention-webhook.api-server")

    val host = config.getString("host")
    val port = config.getInt("port")
    val bidingFuture = Http().bindAndHandle(routes.routes, host, port)

    println(s"Server online at http://${host}:${port}/\nPress RETURN to stop...")
    StdIn.readLine()
    bidingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())

  }
}
