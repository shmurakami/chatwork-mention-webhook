package com.shmrkm.chatworkWebhook.domain.model.auth

import com.shmrkm.chatworkWebhook.domain.model.account.AccountId

case class Authentication(accountId: AccountId, accessToken: AccessToken)
