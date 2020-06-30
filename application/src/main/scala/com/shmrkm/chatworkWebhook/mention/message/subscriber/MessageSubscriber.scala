package com.shmrkm.chatworkWebhook.mention.message.subscriber

import akka.actor.{ Actor, ActorLogging, Props }
import com.redis.{ E, M, PubSubMessage, S, U }
import com.shmrkm.chatworkWebhook.mention.MentionStreamRepositoryFactory
import com.shmrkm.chatworkWebhook.mention.message.subscriber.MessageSubscriber.{ ConsumeError, ConsumedMessage }
import com.shmrkm.chatworkWebhook.mention.message.subscriber.MessageSubscriberProxy.Start
import com.typesafe.config.Config

import scala.concurrent.ExecutionContext

object MessageSubscriber {
  case class ConsumedMessage(message: String)
  case class ConsumeError(ex: Throwable)

  def props = Props(new MessageSubscriber)
  def name  = "message-subscriber"
}

class MessageSubscriber extends Actor with ActorLogging with MentionStreamRepositoryFactory {
  implicit val ec: ExecutionContext = context.system.dispatcher
  implicit val config: Config       = context.system.settings.config

  val mentionRepository = factoryMentionRepository()
  val channelName       = config.getString("redis.channel-name")

  override def receive: Receive = {
    case _: Start => mentionRepository.subscribe(channelName)(registerConsumerReceiver)

    case message: ConsumedMessage => log.info(s"consumed $message")
    case e: ConsumeError          => log.warning(s"error occurred $e")
  }

  def registerConsumerReceiver: PubSubMessage => Unit = {
    case S(channel: String, _) => log.info(s"redis channel $channel subscribed")
    case U(channel: String, _) => log.info(s"unsubscribed redis channel $channel")
    case M(origChannel: String, message: String) =>
      log.info(s"message $message published to channel $origChannel")
      self ! ConsumedMessage(message)
    case E(e) => log.warning(s"subscribing error occurred $e")
  }
}
