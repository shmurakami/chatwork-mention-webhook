package com.shmrkm.chatworkWebhook.mention.mention

import akka.actor.{Actor, ActorLogging, Props}
import com.shmrkm.chatworkWebhook.mention.mention.MentionRecordActor.Record
import com.shmrkm.chatworkWebhook.mention.protocol.command.MentionCommand

object MentionHandlerActor {
  def props = Props(new MentionHandlerActors)

  sealed trait Command
  case class ReceiveMention(command: MentionCommand) extends Command

  sealed trait StoreResult
  case class SuccessToStore()              extends StoreResult
  case class FailureToStore(ex: Throwable) extends StoreResult

}

class MentionHandlerActors extends Actor with ActorLogging {
  import MentionHandlerActor._

  implicit val executionContext = context.dispatcher

  override def receive: Receive = {
    case ReceiveMention(command) => context.actorOf(MentionRecordActor.props(sender())) ! Record(command.message)
  }
}
