package com.shmrkm.chatworkMention.repository

trait MentionStreamRepositoryFactory extends RedisRepositoryFactory {
  def factoryStreamRepository(): StreamRepository = factory()
}
