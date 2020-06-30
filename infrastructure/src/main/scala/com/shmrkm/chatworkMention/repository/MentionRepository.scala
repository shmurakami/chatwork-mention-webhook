package com.shmrkm.chatworkMention.repository

import akka.Done
import com.redis.{ PubSubMessage, _ }
import com.shmrkm.chatworkMention.exception.StoreException
import com.shmrkm.chatworkWebhook.domain.model.account.ToAccountId
import com.shmrkm.chatworkWebhook.domain.model.mention.MentionList
import com.shmrkm.chatworkWebhook.domain.model.message.Message
import com.typesafe.scalalogging.Logger

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success, Try }

trait MentionRepository {
  def publish(message: Message, channelName: String): Future[Try[Boolean]]

  def subscribe(channelName: String)(consumer: PubSubMessage => Unit): Unit

  def resolve(accountId: ToAccountId): Future[MentionList]

  def updateReadModel(toAccountId: ToAccountId, mentionList: MentionList): Future[Try[Done]]
}

class MentionRepositoryRedisImpl(redisClient: RedisClient)(implicit ec: ExecutionContext) extends MentionRepository {

  import io.circe.generic.auto._
  import io.circe.parser._
  import io.circe.syntax._

  private val logger = Logger(classOf[MentionRepository])

  override def publish(message: Message, channelName: String): Future[Try[Boolean]] = {
    Future {
      redisClient.publish(channelName, message.asJson.toString) match {
        case Some(_) => logger.info("succeeded to store"); Success(true)
        case None    => logger.warn("failed to store"); Failure(new StoreException("failed to publish to redis"))
      }
    }
  }

  override def subscribe(channelName: String)(consumer: PubSubMessage => Unit): Unit = {
    logger.info("start subscribe")
    redisClient.subscribe(channelName)(consumer)
  }

  override def resolve(accountId: ToAccountId): Future[MentionList] = Future {
    val stored = redisClient.get(readModelKey(accountId)).getOrElse("""{"list":[]}""")
    parse(stored).right.flatMap(_.as[MentionList]) match {
      case Right(mentionList) => mentionList
      case Left(_) =>
        logger.info(s"empty mention list for account: ${accountId.value}")
        MentionList(Seq.empty)
    }
  }

  override def updateReadModel(toAccountId: ToAccountId, mentionList: MentionList): Future[Try[Done]] = {
    Future {
      if (redisClient.set(readModelKey(toAccountId), mentionList.asJson.toString)) Success(Done)
      else Failure(new StoreException("failed to update read model"))
    }
  }

  private def readModelKey(accountId: ToAccountId): String = {
    val md  = java.security.MessageDigest.getInstance("SHA-1")
    val key = s"read-model-${accountId.value}"
    md.digest(key.getBytes("UTF-8")).map("%02x".format(_)).mkString
  }
}
