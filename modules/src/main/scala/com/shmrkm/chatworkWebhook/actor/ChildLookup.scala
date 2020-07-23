package com.shmrkm.chatworkWebhook.actor

import akka.actor.{ActorContext, ActorRef, Props}

trait ChildLookup {
  implicit val context: ActorContext

  type Command

  def createAndForward(props: Props, name: String)(command: Command) = createSubscriber(props, name) forward command

  def createSubscriber(props: Props, name: String): ActorRef = context.actorOf(props, name)

  def forwardCmd(command: Command)(ref: ActorRef) = ref forward command


}
