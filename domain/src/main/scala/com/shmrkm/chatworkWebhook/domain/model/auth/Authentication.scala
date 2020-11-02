package com.shmrkm.chatworkWebhook.domain.model.auth

import com.shmrkm.chatworkWebhook.domain.model.account.AccountId
import com.shmrkm.chatworkWebhook.domain.model.chatwork.ApiToken

case class Authentication(accountId: AccountId, apiToken: ApiToken, accessToken: AccessToken)
