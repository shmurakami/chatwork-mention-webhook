package com.shmrkm.chatworkMention.repository

import com.redis.RedisClient
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContext

private[repository] trait RedisRepositoryFactory {
  implicit def ec: ExecutionContext

  private val config = ConfigFactory.load("redis")

  def factory() = {
    new MentionRepositoryRedisImpl(
      new RedisClient(config.getString("redis.host"), config.getInt("redis.port"))
    )
  }
}
