package com.shmrkm.chatworkWebhook.readModelUpdater

import akka.Done
import akka.actor.{ActorSystem, CoordinatedShutdown}
import com.shmrkm.chatworkWebhook.mention.subscriber.MessageSubscriberProxy
import com.shmrkm.chatworkWebhook.mention.subscriber.MessageSubscriberProxy.Start
import com.typesafe.config.ConfigFactory
import kamon.Kamon

import scala.concurrent.{ExecutionContext, Future}

object Main extends App {
  Kamon.init()

  val config = ConfigFactory.load()

  implicit val system               = ActorSystem("read-model-updater", config)
  implicit val ec: ExecutionContext = system.dispatcher

  val subscriber = system.actorOf(MessageSubscriberProxy.props, MessageSubscriberProxy.name)
  subscriber ! Start()

  CoordinatedShutdown(system).addTask(CoordinatedShutdown.PhaseBeforeServiceUnbind, "shutdown RMU") { () =>
    Future {
      system.stop(subscriber)
      Done.done()
    }
  }

}
