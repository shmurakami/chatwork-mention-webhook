package com.shmrkm.chatworkMention.repository

import akka.Done
import com.redis.{PubSubMessage, _}
import com.shmrkm.chatworkMention.exception.StoreException
import com.shmrkm.chatworkWebhook.domain.model.account.AccountId
import com.shmrkm.chatworkWebhook.domain.model.mention.MentionList
import com.shmrkm.chatworkWebhook.domain.model.message.Message
import com.typesafe.scalalogging.Logger

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

trait MentionRepository {
  def publish(message: Message, channelName: String): Future[Try[Boolean]]

  def subscribe(channelName: String)(consumer: PubSubMessage => Unit): Unit

  def resolve(accountId: AccountId): Future[MentionList]

  def updateReadModel(toAccountId: AccountId, mentionList: MentionList): Future[Try[Done]]
}

class MentionRepositoryRedisImpl(redisClient: RedisClient)(implicit ec: ExecutionContext) extends MentionRepository {

  import io.circe.generic.auto._
  import io.circe.parser._
  import io.circe.syntax._

  private val logger = Logger(classOf[MentionRepository])

  override def publish(message: Message, channelName: String): Future[Try[Boolean]] = {
    Future {
      redisClient.publish(channelName, message.asJson.toString) match {
        case Some(_) => logger.info("succeeded to publish"); Success(true)
        case None    => logger.warn("failed to publish"); Failure(new StoreException("failed to publish to redis"))
      }
    }
  }

  override def subscribe(channelName: String)(consumer: PubSubMessage => Unit): Unit = {
    logger.info("start subscribe")
    redisClient.subscribe(channelName)(consumer)
  }

  override def resolve(accountId: AccountId): Future[MentionList] = Future {
    val stored = redisClient.get(readModelKey(accountId)).getOrElse("""{"list":[]}""")
    // TODO left
    parse(stored).right.flatMap(_.as[MentionList]) match {
      case Right(mentionList) => mentionList
      case Left(_) =>
        logger.info(s"empty mention list for account: ${accountId.value}")
        MentionList(Seq.empty)
    }
  }

  override def updateReadModel(toAccountId: AccountId, mentionList: MentionList): Future[Try[Done]] = {
    logger.info("do update read model")
    Future {
      if (redisClient.set(readModelKey(toAccountId), mentionList.asJson.toString)) { logger.info("updated read model");Success(Done)}
      else Failure(new StoreException("failed to update read model"))
    }
  }

  private def readModelKey(accountId: AccountId): String = {
    val md  = java.security.MessageDigest.getInstance("SHA-1")
    val key = s"read-model-${accountId.value}"
    md.digest(key.getBytes("UTF-8")).map("%02x".format(_)).mkString
  }
}
