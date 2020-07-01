package com.shmrkm.chatworkWebhook.domain.model.chatwork

import com.shmrkm.chatworkWebhook.domain.model.account.{ AccountId, AccountName }

case class Me(accountId: AccountId, name: AccountName)
