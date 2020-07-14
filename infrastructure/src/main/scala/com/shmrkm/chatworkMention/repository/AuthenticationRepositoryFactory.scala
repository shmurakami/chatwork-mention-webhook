package com.shmrkm.chatworkMention.repository

import com.redis.RedisClient

trait AuthenticationRepositoryFactory extends RedisRepositoryFactory {
  def factoryAuthenticationRepository(): AuthenticationRepository = {
    new AuthenticationRepositoryImpl(new RedisClient(redisConfig.getString("redis.host"), redisConfig.getInt("redis.port")))
  }

}
