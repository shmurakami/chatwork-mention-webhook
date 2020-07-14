package com.shmrkm.chatworkMention.repository

import com.redis.RedisClient
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContext

private[repository] trait RedisRepositoryFactory {
  implicit def ec: ExecutionContext

  private[repository] val redisConfig = ConfigFactory.load("redis")

  private[repository] def factory() = {
    new MentionRepositoryRedisImpl(
      new RedisClient(redisConfig.getString("redis.host"), redisConfig.getInt("redis.port"))
    )
  }
}
