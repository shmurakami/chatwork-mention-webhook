package com.shmrkm.chatworkWebhook.mention.protocol.write

import com.shmrkm.chatworkWebhook.domain.model.account.{ FromAccountId, ToAccountId }
import com.shmrkm.chatworkWebhook.domain.model.message._
import com.shmrkm.chatworkWebhook.domain.model.room._

case class MentionCommand(
    fromAccountId: FromAccountId,
    toAccountId: ToAccountId,
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
