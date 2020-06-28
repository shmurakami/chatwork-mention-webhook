package com.shmrkm.chatworkWebhook.mention

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import com.shmrkm.chatworkWebhook.mention.controller.{ MentionController, WebhookController }
import com.typesafe.config.{ Config, ConfigFactory }

import scala.io.StdIn

object Main {

  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load()

    implicit val system           = ActorSystem("mention-webhook", config)
    implicit val executionContext = system.dispatcher

    // TODO use airframe
    val routes = new Routes(
      new WebhookController,
      new MentionController
    )

    val host         = config.getString("chatwork-mention-webhook.api-server.host")
    val port         = config.getInt("chatwork-mention-webhook.api-server.port")
    val bidingFuture = Http().bindAndHandle(routes.routes, host, port)

    println(s"Server online at http://${host}:${port}/\nPress RETURN to stop...")
    StdIn.readLine()
    bidingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())

  }
}
