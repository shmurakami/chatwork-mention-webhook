package com.shmrkm.chatworkWebhook.interface.adaptor

import com.shmrkm.chatworkWebhook.domain.model.query.message.QueryMessage

trait MentionServiceReplier {
  def queryMessage2MentionReply(queryMessage: QueryMessage): MentionReply = {
    MentionReply(
      id = queryMessage.id.value,
      roomId = queryMessage.roomId.value,
      roomName = queryMessage.roomName.value,
      roomIconUrl = queryMessage.roomIconUrl.value,
      fromAccountId = queryMessage.fromAccountId.value.toInt,
      fromAccountName = queryMessage.fromAccountName.value,
      fromAccountAvatarUrl = queryMessage.fromAccountAvatarUrl.value,
      toAccountId = queryMessage.toAccountId.value.toInt,
      body = queryMessage.body.value,
      sendTime = queryMessage.sendTime.value,
      updateTime = queryMessage.updateTime.value
    )
  }
}
