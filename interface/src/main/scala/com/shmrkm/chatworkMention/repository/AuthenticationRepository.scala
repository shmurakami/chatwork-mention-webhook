package com.shmrkm.chatworkMention.repository

import com.shmrkm.chatworkWebhook.domain.model.account.AccountId
import com.shmrkm.chatworkWebhook.domain.model.auth.{AccessToken, Authentication}

import scala.concurrent.Future
import scala.util.Try

trait AuthenticationRepository {

  def resolve(accountId: AccountId): Future[Either[Throwable, Authentication]]

  def issueAccessToken(authentication: Authentication): Future[Try[AccessToken]]

}
