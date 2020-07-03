package com.shmrkm.chatworkMention.repository

import com.redis.RedisClient
import com.typesafe.config.Config
import com.typesafe.scalalogging.Logger

import scala.concurrent.ExecutionContext

trait MentionStreamRepositoryFactory {
  implicit def ec: ExecutionContext

  implicit def config: Config

  def factoryMentionRepository() = {
    val c = new RedisClient(config.getString("redis.host"), config.getInt("redis.port"))

    val logger = Logger(classOf[MentionStreamRepositoryFactory])
    logger.info(c.get("4a3affb7c73c94618bdf81e7d973e7038f734b97").getOrElse("empty"))

    new MentionRepositoryRedisImpl(
      new RedisClient(config.getString("redis.host"), config.getInt("redis.port"))
    )
  }
}
