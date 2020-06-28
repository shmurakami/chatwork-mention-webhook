package com.shmrkm.chatworkWebhook.mention.mention

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.redis.RedisClient
import com.shmrkm.chatworkMention.repository.MentionRepositoryRedisImpl
import com.shmrkm.chatworkWebhook.domain.model.message.Message
import com.shmrkm.chatworkWebhook.mention.mention.MentionHandlerActor.{FailureToStore, SuccessToStore}
import com.shmrkm.chatworkWebhook.mention.mention.MentionRecordActor.Record

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object MentionRecordActor {
  case class Record(command: Message)

  def props(refTo: ActorRef) = Props(new MentionRecordActor(refTo))
}

class MentionRecordActor(refTo: ActorRef) extends Actor with ActorLogging {
  implicit val executionContext: ExecutionContext = context.dispatcher

  val redisConfig = context.system.settings.config.getConfig("redis")
  val channelName = redisConfig.getString("channel-name")

  val mentionRepository = new MentionRepositoryRedisImpl(
    new RedisClient(redisConfig.getString("host"), redisConfig.getInt("port"))
  )

  override def receive: Receive = {
    case Record(message) => refTo forward mentionRepository.store(message, channelName).map {
      case Success(_) => SuccessToStore()
      case Failure(ex: Exception) => FailureToStore(ex)
    }
  }
}
