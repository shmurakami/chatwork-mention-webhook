package com.shmrkm.chatworkWebhook.mention

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import com.shmrkm.chatworkWebhook.mention.controller.{AuthenticationController, MentionController, WebhookController}
import com.shmrkm.chatworkWebhook.mention.message.subscriber.MessageSubscriberProxy
import com.shmrkm.chatworkWebhook.mention.message.subscriber.MessageSubscriberProxy.Start
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Await, Future}

object Main {

  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load()

    implicit val system           = ActorSystem("mention-webhook", config)
    implicit val executionContext = system.dispatcher

    // start read-model-updater
    // TODO run it as another project
    val subscriber = system.actorOf(MessageSubscriberProxy.props, MessageSubscriberProxy.name)
    subscriber ! Start()

    // TODO use airframe
    val routes = new Routes(
      new AuthenticationController,
      new WebhookController,
      new MentionController
    )

    val host          = config.getString("chatwork-mention-webhook.api-server.host")
    val port          = config.getInt("chatwork-mention-webhook.api-server.port")
    val bindingFuture = Http().bindAndHandle(routes.routes, host, port)

    val terminationDuration = FiniteDuration(config.getDuration("chatwork-mention-webhook.api-server.termination-duration").toMillis, TimeUnit.MILLISECONDS)

    sys.addShutdownHook {
      val future = Future
        .successful()
        .flatMap(_ => bindingFuture.flatMap(_.unbind()))
      Await.result(future, terminationDuration)
    }
  }
}
