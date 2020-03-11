package com.shmrkm.chatworkMention.repository

import com.shmrkm.chatworkWebhook.domain.model.ToAccountId
import com.redis._
import com.shmrkm.chatworkWebhook.domain.model.mention.MentionList

import scala.concurrent.{ExecutionContext, Future}

trait MentionRepository {
  def store(accountId: ToAccountId, list: MentionList): Boolean
  def resolve(accountId: ToAccountId): Future[MentionList]
}

class MentionRepositoryRedisImpl(redisClient: RedisClient)(implicit ec: ExecutionContext) extends MentionRepository {

  override def store(accountId: ToAccountId, list: MentionList): Boolean = {
    import io.circe.generic.auto._, io.circe.syntax._

    redisClient.set(readModelKey(accountId), list.asJson)
  }

  override def resolve(accountId: ToAccountId): Future[MentionList] = Future {
    MentionList(Seq.empty)
  }

  private def readModelKey(accountId: ToAccountId): String = {
    val md = java.security.MessageDigest.getInstance("SHA-1")
    val key = s"read-model-${accountId.value}"
    md.digest(key.getBytes("UTF-8")).map("%02x".format(_)).mkString
  }
}
