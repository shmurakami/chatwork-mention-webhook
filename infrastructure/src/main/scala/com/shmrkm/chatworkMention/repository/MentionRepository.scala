package com.shmrkm.chatworkMention.repository

import akka.Done
import com.redis.{PubSubMessage, _}
import com.shmrkm.chatworkMention.exception.StoreException
import com.shmrkm.chatworkWebhook.domain.model.account.AccountId
import com.shmrkm.chatworkWebhook.domain.model.mention.MentionList
import com.shmrkm.chatworkWebhook.domain.model.message.Message
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.Logger

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

trait StreamRepository {
  def publish(message: Message): Future[Try[Boolean]]

  def subscribe(consumer: PubSubMessage => Unit): Unit
}

trait MentionRepository {
  def resolve(accountId: AccountId): Future[MentionList]

  def updateReadModel(toAccountId: AccountId, mentionList: MentionList): Future[Try[Done]]
}

class MentionRepositoryRedisImpl(redisClient: RedisClient)(implicit ec: ExecutionContext) extends StreamRepository with MentionRepository {

  import io.circe.generic.auto._
  import io.circe.parser._
  import io.circe.syntax._

  private val logger = Logger(classOf[MentionRepository]);
  private val config = ConfigFactory.load("redis")

  override def publish(message: Message): Future[Try[Boolean]] = Future {
    val channelName = resolveStreamChannelName()
    redisClient.publish(channelName, message.asJson.noSpaces) match {
      case Some(_) => logger.info(s"succeeded to publish to channel $channelName"); Success(true)
      case None => logger.warn("failed to publish"); Failure(new StoreException("failed to publish to redis"))
    }
  }

  override def subscribe(consumer: PubSubMessage => Unit): Unit = {
    logger.info("start subscribe")
    redisClient.subscribe(resolveStreamChannelName())(consumer)
  }

  private def resolveStreamChannelName(): String = config.getString("redis.channel-name")

  override def resolve(accountId: AccountId): Future[MentionList] = Future {
    val default = MentionList(Seq.empty)
    redisClient.get(readModelKey(accountId)) match {
      case Some(mention) =>
        parse(mention) match {
          case Left(error) =>
            logger.warn(s"Parsing json error. Invalid format json stored $error")
            default
          case Right(json) => json.as[MentionList] match {
            case Left(error) =>
              logger.warn(s"Decoding json failure $error")
              default
            case Right(mentionList) => mentionList
          }
        }

      case None =>
        logger.info(s"mention is empty for account $accountId")
        default
    }
  }

  override def updateReadModel(toAccountId: AccountId, mentionList: MentionList): Future[Try[Done]] = {
    Future {
      if (redisClient.set(readModelKey(toAccountId), mentionList.asJson.noSpaces)) Success(Done)
      else Failure(new StoreException("failed to update read model"))
    }
  }

  private def readModelKey(accountId: AccountId): String = {
    val md  = java.security.MessageDigest.getInstance("SHA-1")
    val key = s"read-model-${accountId.value}"
    md.digest(key.getBytes("UTF-8")).map("%02x".format(_)).mkString
  }
}
