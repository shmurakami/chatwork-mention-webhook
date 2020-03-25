package com.shmrkm.chatworkWebhook.domain.model.account

case class ToAccount(
  accountId: ToAccountId,
  accountName: AccountName,
  accountAvatarUrl: FromAccountAvatarUrl
)
