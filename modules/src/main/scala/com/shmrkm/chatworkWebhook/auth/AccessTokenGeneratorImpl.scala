package com.shmrkm.chatworkWebhook.auth

import com.shmrkm.chatworkMention.accessToken.AccessTokenGenerator
import com.shmrkm.chatworkMention.hash.TokenGenerator
import com.shmrkm.chatworkWebhook.domain.model.account.AccountId
import com.shmrkm.chatworkWebhook.domain.model.auth.AccessToken

class AccessTokenGeneratorImpl extends AccessTokenGenerator {

  override def generate(accountId: AccountId): AccessToken = authKey(accountId)

  private def authKey(accountId: AccountId): AccessToken = {
    val tokenGenerator = new TokenGenerator
    // TODO check it works. redis has key not converted?
    AccessToken(tokenGenerator.generateSHAToken(s"authentication-${accountId.value}"))
  }

}
