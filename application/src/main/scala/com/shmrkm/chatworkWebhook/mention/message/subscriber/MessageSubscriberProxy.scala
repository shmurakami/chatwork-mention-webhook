package com.shmrkm.chatworkWebhook.mention.message.subscriber

import akka.actor.{Actor, ActorLogging, Props}
import com.shmrkm.chatworkWebhook.mention.message.subscriber.MessageSubscriberProxy.Start

object MessageSubscriberProxy {
  case object Start

  def props = Props(new MessageSubscriberProxy)
}

/**
 * whats to do
 * run consumer via supervisor
 * start subscribing redis channel
 *
 * subscriber
 * get message
 * call chatwork api to retrieve room name, account name, icon url
 * update redis read model
 * notification to FCM
 */
class MessageSubscriberProxy extends Actor with ActorLogging {
  override def receive: Receive = {
    case Start => log.info("start subscribing")
  }
}
