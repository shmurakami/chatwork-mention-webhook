package com.shmrkm.chatworkWebhook.domain.model.query.message

import com.shmrkm.chatworkWebhook.domain.model.account.{AccountId, AccountName, FromAccountAvatarUrl}
import com.shmrkm.chatworkWebhook.domain.model.message.{MessageBody, MessageId, SendTime, UpdateTime}
import com.shmrkm.chatworkWebhook.domain.model.room.{RoomIconUrl, RoomId, RoomName}

case class QueryMessage(
    id: MessageId,
    roomId: RoomId,
    roomName: RoomName,
    roomIconUrl: RoomIconUrl,
    fromAccountId: AccountId,
    fromAccountName: AccountName,
    fromAccountAvatarUrl: FromAccountAvatarUrl,
    toAccountId: AccountId,
    body: MessageBody,
    sendTime: SendTime,
    updateTime: UpdateTime
)
