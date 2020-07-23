package com.shmrkm.chatworkWebhook.actor

import akka.actor.{ActorContext, ActorRef, Props}

trait ChildLookup {
  implicit val context: ActorContext

  type Command

  def createAndForward(props: Props, name: String)(command: Command) = createChild(props, name) forward command

  def createChild(props: Props, name: String): ActorRef = context.actorOf(props, name)

  def forwardCmd(command: Command)(ref: ActorRef) = ref forward command


}
