package com.shmrkm.chatworkWebhook.mention

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import com.shmrkm.chatworkWebhook.mention.controller.{MentionController, WebhookController}
import com.shmrkm.chatworkWebhook.mention.message.subscriber.MessageSubscriberProxy
import com.shmrkm.chatworkWebhook.mention.message.subscriber.MessageSubscriberProxy.Start
import com.typesafe.config.{Config, ConfigFactory}

import scala.io.StdIn

object Main {

  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load()

    implicit val system           = ActorSystem("mention-webhook", config)
    implicit val executionContext = system.dispatcher

    // start read-model-updater
    // TODO run it as another project
    val subscriber = system.actorOf(MessageSubscriberProxy.props, "message-subscriber")
    subscriber ! Start

    // TODO use airframe
    val routes = new Routes(
      new WebhookController,
      new MentionController
    )

    val host         = config.getString("chatwork-mention-webhook.api-server.host")
    val port         = config.getInt("chatwork-mention-webhook.api-server.port")
    val bidingFuture = Http().bindAndHandle(routes.routes, host, port)

    // TODO revise finish processes
    println(s"Server online at http://${host}:${port}/\nPress RETURN to stop...")
    StdIn.readLine()
    bidingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }
}
