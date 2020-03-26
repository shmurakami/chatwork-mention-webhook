package com.shmrkm.chatworkWebhook.mention.protocol

import com.shmrkm.chatworkWebhook.domain.model.account.{ FromAccountId, ToAccountId }
import com.shmrkm.chatworkWebhook.domain.model.message.{ Message, MessageBody, MessageId, SendTime, UpdateTime }
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

  def message: Message = Message(messageId, body, sendTime, updateTime)
}
