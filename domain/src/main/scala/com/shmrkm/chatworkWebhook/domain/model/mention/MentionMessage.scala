package com.shmrkm.chatworkWebhook.domain.model.mention

import com.shmrkm.chatworkWebhook.domain.model.account.{AccountId, FromAccountAvatarUrl}
import com.shmrkm.chatworkWebhook.domain.model.message.{MessageBody, MessageId, SendTime, UpdateTime}
import com.shmrkm.chatworkWebhook.domain.model.room.{RoomIconUrl, RoomId, RoomName}

case class MentionMessage(
    fromAccountId: AccountId,
    fromAccountAvatarUrl: FromAccountAvatarUrl,
    roomId: RoomId,
    roomName: RoomName,
    roomIconUrl: RoomIconUrl,
    messageId: MessageId,
    body: MessageBody,
    sendTime: SendTime,
    updateTime: UpdateTime
)
