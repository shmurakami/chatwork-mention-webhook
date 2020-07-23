package com.shmrkm.chatworkWebhook.mention.subscriber

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import com.shmrkm.chatworkWebhook.actor.ChildLookup
import com.shmrkm.chatworkWebhook.mention.subscriber.MessageSubscriberProxy.Start

object MessageSubscriberProxy {
  sealed trait Command
  case class Start() extends Command

  def props = Props(new MessageSubscriberProxy)

  def name = "message-subscriber-proxy"
}

class MessageSubscriberProxy extends Actor with ActorLogging with ChildLookup {
  override type Command = MessageSubscriberProxy.Command

  override def receive: Receive = {
    case cmd: Start =>
      val childRef = createChild(MessageSubscriber.props, MessageSubscriber.name)
      childRef forward cmd
      context.watch(childRef)

    case Terminated(_) => self ! Start
  }

}
