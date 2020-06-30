package com.shmrkm.chatworkWebhook.domain.model.message

import com.shmrkm.chatworkWebhook.domain.model.account.{FromAccountId, ToAccount, ToAccountId}
import com.shmrkm.chatworkWebhook.domain.model.room.RoomId

case class Message(
    id: MessageId,
    roomId: RoomId,
    fromAccountId: FromAccountId,
    toAccountId: ToAccountId,
    body: MessageBody,
    sendTime: SendTime,
    updateTime: UpdateTime
)
