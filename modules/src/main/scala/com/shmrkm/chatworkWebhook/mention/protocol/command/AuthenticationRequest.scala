package com.shmrkm.chatworkWebhook.mention.protocol.command

import com.shmrkm.chatworkWebhook.domain.model.account.AccountId
import com.shmrkm.chatworkWebhook.domain.model.chatwork.ApiToken

case class AuthenticationRequest(account_id: Int, token: String) {
  def command = AuthenticationCommand(AccountId(account_id), ApiToken(token))
}
