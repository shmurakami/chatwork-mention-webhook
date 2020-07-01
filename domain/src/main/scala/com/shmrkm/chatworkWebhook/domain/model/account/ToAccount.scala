package com.shmrkm.chatworkWebhook.domain.model.account

case class ToAccount(
    accountId: AccountId,
    accountName: AccountName,
    accountAvatarUrl: FromAccountAvatarUrl
)
