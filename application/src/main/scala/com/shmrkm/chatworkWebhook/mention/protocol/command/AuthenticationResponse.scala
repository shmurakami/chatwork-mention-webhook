package com.shmrkm.chatworkWebhook.mention.protocol.command

import com.shmrkm.chatworkWebhook.domain.model.account.AccountId
import com.shmrkm.chatworkWebhook.domain.model.auth.AccessToken

case class AuthenticationResponse(account_id: AccountId, token: AccessToken)
