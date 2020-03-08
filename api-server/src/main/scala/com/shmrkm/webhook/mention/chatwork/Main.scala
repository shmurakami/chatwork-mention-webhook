package com.shmrkm.webhook.mention.chatwork

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._
import com.shmrkm.webhook.mention.chatwork.controller.WebhookController
import com.typesafe.config.Config
import com.webhook.mention.chatwork.useCase.WebhookUseCase

import scala.io.StdIn

object Main {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("webhook")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val routes = new Routes(
      new WebhookController(new WebhookUseCase)
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
