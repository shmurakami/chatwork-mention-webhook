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
  def fetch(accountId: AccountId): Future[MentionList]

  def updateReadModel(toAccountId: AccountId, mentionList: MentionList): Try[Done]
}

class MentionRepositoryRedisImpl(redisClient: RedisClient)(implicit ec: ExecutionContext) extends StreamRepository with MentionRepository {

  import io.circe.generic.auto._
  import io.circe.parser._
  import io.circe.syntax._

  private val logger = Logger(classOf[MentionRepository]);
  private val config = ConfigFactory.load("redis")

  override def publish(message: Message): Future[Try[Boolean]] = Future {
    val channelName = resolveStreamChannelName()
    val result = redisClient.publish(channelName, message.asJson.noSpaces) match {
      case Some(_) => Success(true)
      case None => Failure(new StoreException("failed to publish to redis"))
    }
    // TODO keep connection
    redisClient.close()
    result
  }

  override def subscribe(consumer: PubSubMessage => Unit): Unit = {
    redisClient.subscribe(resolveStreamChannelName())(consumer)
  }

  private def resolveStreamChannelName(): String = config.getString("redis.channel-name")

  override def fetch(accountId: AccountId): Future[MentionList] = {
    // TODO how to keep connection? need thread pool?
    val f = resolve(accountId)
    redisClient.close()
    f
  }

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

      case None => default
    }
  }

  override def updateReadModel(toAccountId: AccountId, mentionList: MentionList): Try[Done] = {
    // why mention list can be decoded by circe?
    if (redisClient.set(readModelKey(toAccountId), mentionList.asJson.noSpaces)) Success(Done)
    else Failure(new StoreException("failed to update read model"))
  }

  private def readModelKey(accountId: AccountId): String = {
    val md  = java.security.MessageDigest.getInstance("SHA-1")
    val key = s"read-model-${accountId.value}"
    md.digest(key.getBytes("UTF-8")).map("%02x".format(_)).mkString
  }
}
