package com.shmrkm.chatworkMention.repository

trait MentionRepositoryFactory extends RedisRepositoryFactory {
  def factoryMentionRepository(): MentionRepository = factory()
}
