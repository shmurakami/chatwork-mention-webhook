package com.shmrkm.chatworkWebhook.readModelUpdater

import akka.Done
import akka.actor.{ ActorSystem, CoordinatedShutdown }
import com.shmrkm.chatworkWebhook.mention.message.subscriber.MessageSubscriberProxy
import com.shmrkm.chatworkWebhook.mention.message.subscriber.MessageSubscriberProxy.Start
import com.typesafe.config.ConfigFactory

import scala.concurrent.{ ExecutionContext, Future }

object Main extends App {

  override def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load("read-model-updater")

    implicit val system               = ActorSystem("read-model-updater", config)
    implicit val ec: ExecutionContext = system.dispatcher

    val subscriber = system.actorOf(MessageSubscriberProxy.props, MessageSubscriberProxy.name)
    subscriber ! Start()

    CoordinatedShutdown(system).addTask(CoordinatedShutdown.PhaseBeforeServiceUnbind, "shutdown http server") { () =>
      Future {
        system.stop(subscriber)
        Done.done()
      }
    }
  }

}
