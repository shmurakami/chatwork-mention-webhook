package com.shmrkm.chatworkMention.accessToken

import com.shmrkm.chatworkWebhook.domain.model.account.AccountId
import com.shmrkm.chatworkWebhook.domain.model.auth.AccessToken

trait AccessTokenGenerator {

  def generate(accountId: AccountId): AccessToken

}
