package com.shmrkm.chatworkWebhook.domain.model.query.message

import com.shmrkm.chatworkWebhook.domain.model.account.{ AccountName, FromAccountAvatarUrl, FromAccountId }
import com.shmrkm.chatworkWebhook.domain.model.message.{ MessageBody, MessageId, SendTime, UpdateTime }
import com.shmrkm.chatworkWebhook.domain.model.room.{ RoomIconUrl, RoomId, RoomName }

case class QueryMessage(
    id: MessageId,
    roomId: RoomId,
    roomName: RoomName,
    roomIconUrl: RoomIconUrl,
    fromAccountId: FromAccountId,
    fromAccountName: AccountName,
    fromAccountAvatarUrl: FromAccountAvatarUrl,
    body: MessageBody,
    sendTime: SendTime,
    updateTime: UpdateTime
)
