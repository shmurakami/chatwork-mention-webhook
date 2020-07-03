package com.shmrkm.chatworkMention.repository

import com.shmrkm.chatworkWebhook.domain.model.auth.{AccessToken, Authentication}

import scala.concurrent.Future

trait AuthenticationRepository {

  def resolve(accessToken: AccessToken): Future[Option[Authentication]]

  def issueAccessToken(authentication: Authentication): Future[AccessToken]

}
