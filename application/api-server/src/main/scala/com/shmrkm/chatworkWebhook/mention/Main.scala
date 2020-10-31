package com.shmrkm.chatworkWebhook.mention

import java.util.concurrent.TimeUnit

import akka.Done
import akka.actor.{ActorSystem, CoordinatedShutdown}
import akka.http.scaladsl.Http
import com.shmrkm.chatworkWebhook.mention.controller.{AuthenticationController, MentionController, WebhookController}
import com.typesafe.config.ConfigFactory
import kamon.Kamon

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

object Main {
//  Kamon.init()

  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load()

    implicit val system           = ActorSystem("mention-webhook", config)
    implicit val executionContext = system.dispatcher

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

    CoordinatedShutdown(system).addTask(CoordinatedShutdown.PhaseBeforeServiceUnbind, "shutdown http server") { () =>
      Future {
        bindingFuture.flatMap(_.terminate(terminationDuration))
        Done.done()
      }
    }
  }
}
