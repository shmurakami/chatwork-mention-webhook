package com.shmrkm.chatworkWebhook.mention.mention

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import com.shmrkm.chatworkWebhook.domain.model.message.Message
import com.shmrkm.chatworkWebhook.mention.MentionStreamRepositoryFactory
import com.shmrkm.chatworkWebhook.mention.mention.MentionHandlerActor.{FailureToStore, SuccessToStore}
import com.shmrkm.chatworkWebhook.mention.mention.MentionRecordActor.Record
import com.typesafe.config.Config

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object MentionRecordActor {
  case class Record(command: Message)

  def props(refTo: ActorRef) = Props(new MentionRecordActor(refTo))
}

class MentionRecordActor(refTo: ActorRef) extends Actor with ActorLogging with MentionStreamRepositoryFactory {
  implicit val ec: ExecutionContext = context.system.dispatcher
  implicit val config: Config = context.system.settings.config

  val mentionRepository = factoryMentionRepository()
  val channelName = config.getString("redis.channel-name")

  override def receive: Receive = {
    case Record(message) => refTo forward mentionRepository.store(message, channelName).map {
      case Success(_) => SuccessToStore()
      case Failure(ex: Exception) => FailureToStore(ex)
    }
  }
}
