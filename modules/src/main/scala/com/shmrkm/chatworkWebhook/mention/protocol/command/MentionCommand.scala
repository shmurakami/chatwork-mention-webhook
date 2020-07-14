package com.shmrkm.chatworkWebhook.mention.protocol.command

import com.shmrkm.chatworkWebhook.domain.model.account.AccountId
import com.shmrkm.chatworkWebhook.domain.model.message._
import com.shmrkm.chatworkWebhook.domain.model.room.RoomId

case class MentionCommand(
    fromAccountId: AccountId,
    toAccountId: AccountId,
    roomId: RoomId,
    messageId: MessageId,
    body: MessageBody,
    sendTime: SendTime,
    updateTime: UpdateTime
) {

  def message: Message =
    Message(
      id = messageId,
      roomId = roomId,
      fromAccountId = fromAccountId,
      toAccountId = toAccountId,
      body = body,
      sendTime = sendTime,
      updateTime = updateTime
    )
}
