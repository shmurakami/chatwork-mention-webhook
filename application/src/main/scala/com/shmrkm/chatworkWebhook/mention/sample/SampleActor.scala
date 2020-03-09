package com.shmrkm.chatworkWebhook.mention.sample

import akka.actor.{Actor, Props}

object SampleActor {
  def props: Props = Props(new SampleActor)
}

case class SampleRequest()

case class Chain()

class SampleActor extends Actor {
  override def receive: Receive = {
    case SampleRequest => self ! Chain
    case Chain =>
  }
}
