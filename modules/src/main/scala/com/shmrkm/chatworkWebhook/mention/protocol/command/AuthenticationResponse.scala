package com.shmrkm.chatworkWebhook.mention.protocol.command

import com.shmrkm.chatworkWebhook.domain.model.account.AccountId
import com.shmrkm.chatworkWebhook.domain.model.auth.AccessToken

sealed trait AuthenticationResponse

case class SuccessAuthenticationResponse(account_id: AccountId, token: AccessToken) extends AuthenticationResponse
case class FailureAuthenticationResponse(reason: String) extends AuthenticationResponse
