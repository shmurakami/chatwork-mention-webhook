package com.shmrkm.chatworkWebhook.mention.mention

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.shmrkm.chatworkMention.repository.MentionStreamRepositoryFactory
import com.shmrkm.chatworkWebhook.domain.model.message.Message
import com.shmrkm.chatworkWebhook.mention.mention.MentionHandlerActor.{FailureToStore, SuccessToStore}
import com.shmrkm.chatworkWebhook.mention.mention.MentionRecordActor.Record

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object MentionRecordActor {
  case class Record(command: Message)

  def name = "mention-record"
  def props(refTo: ActorRef) = Props(new MentionRecordActor(refTo))
}

class MentionRecordActor(refTo: ActorRef) extends Actor with ActorLogging with MentionStreamRepositoryFactory {
  implicit val ec: ExecutionContext = context.system.dispatcher

  private val mentionRepository = factoryStreamRepository()

  override def receive: Receive = {
    case Record(message) => refTo forward mentionRepository.publish(message).map {
      case Success(_) => SuccessToStore()
      case Failure(ex: Throwable) => FailureToStore(ex)
    }
  }
}
