package com.shmrkm.chatworkMention.repository

import com.shmrkm.chatworkWebhook.domain.model.account.AccountId
import com.shmrkm.chatworkWebhook.domain.model.auth.AccessToken

import scala.concurrent.Future

trait AuthenticationRepository {

  def resolve(accessToken: AccessToken): Option[AccountId]

  def issueAccessToken(accountId: AccountId): Future[AccessToken]

}
