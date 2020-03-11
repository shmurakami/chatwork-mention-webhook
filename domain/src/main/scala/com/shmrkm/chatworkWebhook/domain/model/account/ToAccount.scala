package com.shmrkm.chatworkWebhook.domain.model.account

import com.shmrkm.chatworkWebhook.domain.model.{AccountName, ToAccountId}

case class ToAccount(
  accountId: ToAccountId,
  accountName: AccountName,
  accountAvatarUrl: FromAccountAvatarUrl
)
