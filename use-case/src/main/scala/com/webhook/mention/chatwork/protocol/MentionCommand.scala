package com.webhook.mention.chatwork.protocol

import com.shmrkm.chatworkWebhook.domain.model._

case class MentionCommand(
                           fromAccountId: FromAccountId,
                           roomId: RoomId,
                           messageId: MessageId,
                           body: MessageBody,
                           sendTime: SendTime,
                           updateTime: UpdateTime,
                         )
