package com.shmrkm.chatworkWebhook.domain.model.message

import com.shmrkm.chatworkWebhook.domain.model.account.AccountId
import com.shmrkm.chatworkWebhook.domain.model.room.RoomId

case class Message(
    id: MessageId,
    roomId: RoomId,
    fromAccountId: AccountId,
    toAccountId: AccountId,
    body: MessageBody,
    sendTime: SendTime,
    updateTime: UpdateTime
)
