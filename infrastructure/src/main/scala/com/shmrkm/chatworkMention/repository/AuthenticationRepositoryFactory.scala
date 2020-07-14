package com.shmrkm.chatworkMention.repository

import com.redis.RedisClient

trait AuthenticationRepositoryFactory extends RedisRepositoryFactory {
  def factoryAuthenticationRepository(): AuthenticationRepository = {
    new AuthenticationRepositoryImpl(new RedisClient(config.getString("host"), config.getInt("port")))
  }

}
