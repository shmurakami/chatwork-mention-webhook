package com.shmrkm.chatworkWebhook.mention.protocol.command

import com.shmrkm.chatworkWebhook.domain.model.account.AccountId
import com.shmrkm.chatworkWebhook.domain.model.chatwork.ApiToken

case class AuthenticationCommand(account_id: AccountId, token: ApiToken)
