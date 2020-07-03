package com.shmrkm.chatworkWebhook.mention.message.subscriber

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.shmrkm.chatworkWebhook.mention.message.subscriber.MessageSubscriberProxy.Command

object MessageSubscriberProxy {
  sealed trait Command
  case class Start() extends Command

  def props = Props(new MessageSubscriberProxy)

  def name = "message-subscriber-proxy"
}

class MessageSubscriberProxy extends Actor with ActorLogging {

  override def receive: Receive = {
    case cmd: Command => context.child(MessageSubscriber.name).fold(createAndForward(cmd))(forwardCmd(cmd))
  }

  private def createAndForward(command: Command) = createSubscriber() forward command

  private def createSubscriber(): ActorRef = context.actorOf(MessageSubscriber.props, MessageSubscriber.name)

  private def forwardCmd(command: Command)(ref: ActorRef) = ref forward command
}
