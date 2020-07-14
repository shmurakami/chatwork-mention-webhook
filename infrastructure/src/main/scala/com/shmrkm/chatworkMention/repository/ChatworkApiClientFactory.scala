package com.shmrkm.chatworkMention.repository

import akka.actor.ActorSystem
import com.shmrkm.chatworkWebhook.domain.model.chatwork.ApiToken
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.ExecutionContext

trait ChatworkApiClientFactory {
  implicit val system: ActorSystem
  implicit val ec: ExecutionContext

  private val chatworkApiConfig: Config = ConfigFactory.load("chatwork_api")

  def factoryChatworkApiClient(): ChatworkApiRepository =
    new ChatworkApiRepositoryImpl(
      chatworkApiConfig.getString("chatwork.api.url"),
      ApiToken(chatworkApiConfig.getString("chatwork.api.token"))
    )

}
