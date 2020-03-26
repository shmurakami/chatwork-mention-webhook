package com.shmrkm.chatworkWebhook.domain.model.mention

import com.shmrkm.chatworkWebhook.domain.model.account.{ FromAccountAvatarUrl, FromAccountId }
import com.shmrkm.chatworkWebhook.domain.model.message.{ MessageBody, MessageId, SendTime, UpdateTime }
import com.shmrkm.chatworkWebhook.domain.model.room.{ RoomIconUrl, RoomId, RoomName }
import com.shmrkm.chatworkWebhook.domain.model.message.UpdateTime

case class MentionMessage(
    fromAccountId: FromAccountId,
    fromAccountAvatarUrl: FromAccountAvatarUrl,
    roomId: RoomId,
    roomName: RoomName,
    roomIconUrl: RoomIconUrl,
    messageId: MessageId,
    body: MessageBody,
    sendTime: SendTime,
    updateTime: UpdateTime
)
