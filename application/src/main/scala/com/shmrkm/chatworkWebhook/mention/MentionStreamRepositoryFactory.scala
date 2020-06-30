package com.shmrkm.chatworkWebhook.mention

import com.redis.RedisClient
import com.shmrkm.chatworkMention.repository.MentionRepositoryRedisImpl
import com.typesafe.config.Config

import scala.concurrent.ExecutionContext

trait MentionStreamRepositoryFactory {
  implicit def ec: ExecutionContext

  implicit def config: Config

  def factoryMentionRepository() = new MentionRepositoryRedisImpl(
    new RedisClient(config.getString("redis.host"), config.getInt("redis.port"))
  )
}
