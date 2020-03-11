package com.shmrkm.chatworkWebhook.mention.protocol

import com.shmrkm.chatworkWebhook.domain.model._
import com.shmrkm.chatworkWebhook.domain.model.room._

case class MentionCommand(
  fromAccountId: FromAccountId,
  toAccountId: ToAccountId,
  roomId: RoomId,
  messageId: MessageId,
  body: MessageBody,
  sendTime: SendTime,
  updateTime: UpdateTime,
)
