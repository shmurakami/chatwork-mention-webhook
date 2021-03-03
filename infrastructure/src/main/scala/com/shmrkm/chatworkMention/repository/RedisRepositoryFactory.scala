package com.shmrkm.chatworkMention.repository

import com.redis.RedisClient
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContext

// TODO redis repository should be redis client to persist connection
// maybe actor is more better
private[repository] trait RedisRepositoryFactory {
  implicit def ec: ExecutionContext

  private[repository] val redisConfig = ConfigFactory.load("redis")

  private[repository] def factory() = {
    new MentionRepositoryRedisImpl(
      new RedisClient(redisConfig.getString("redis.host"), redisConfig.getInt("redis.port"))
    )
  }
}
