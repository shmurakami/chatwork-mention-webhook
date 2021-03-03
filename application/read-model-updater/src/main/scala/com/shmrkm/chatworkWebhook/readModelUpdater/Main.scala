package com.shmrkm.chatworkWebhook.readModelUpdater

import akka.Done
import akka.actor.{ ActorSystem, CoordinatedShutdown }
import com.shmrkm.chatworkWebhook.mention.subscriber.MessageSubscriberProxy
import com.shmrkm.chatworkWebhook.mention.subscriber.MessageSubscriberProxy.Start
import com.typesafe.config.ConfigFactory

import scala.concurrent.{ ExecutionContext, Future }

object Main extends App {

  val config = ConfigFactory.load()

  // TODO typed
  implicit val system               = ActorSystem("read-model-updater", config)
  implicit val ec: ExecutionContext = system.dispatcher

  val subscriber = system.actorOf(MessageSubscriberProxy.props, MessageSubscriberProxy.name)
  subscriber ! Start()

  // TODO check
  CoordinatedShutdown(system).addTask(CoordinatedShutdown.PhaseBeforeServiceUnbind, "shutdown RMU") { () =>
    Future {
      system.stop(subscriber)
      Done.done()
    }
  }

}
