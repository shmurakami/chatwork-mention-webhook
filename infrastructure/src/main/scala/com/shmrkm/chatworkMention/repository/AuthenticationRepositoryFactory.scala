package com.shmrkm.chatworkMention.repository

import com.redis.RedisClient
import com.typesafe.config.Config

import scala.concurrent.ExecutionContext

trait AuthenticationRepositoryFactory {
  implicit def ec: ExecutionContext

  def factoryAuthenticationRepository(config: Config): AuthenticationRepository = {
    new AuthenticationRepositoryImpl(new RedisClient(config.getString("redis.host"), config.getInt("redis.port")))
  }

}
