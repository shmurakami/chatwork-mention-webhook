package com.shmrkm.chatworkWebhook.domain.model.mention

import com.shmrkm.chatworkWebhook.domain.model.account.FromAccountAvatarUrl
import com.shmrkm.chatworkWebhook.domain.model.room.{RoomIconUrl, RoomId, RoomName}
import com.shmrkm.chatworkWebhook.domain.model.{FromAccountId, MessageBody, MessageId, SendTime, UpdateTime}

case class MentionMessage(
  fromAccountId: FromAccountId,
  fromAccountAvatarUrl: FromAccountAvatarUrl,
  roomId: RoomId,
  roomName: RoomName,
  roomIconUrl: RoomIconUrl,
  messageId: MessageId,
  body: MessageBody,
  sendTime: SendTime,
  updateTime: UpdateTime,
)

