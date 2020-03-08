package com.webhook.mention.chatwork.protocol

import com.shmrkm.chatworkWebhook.domain.model._

case class WebhookRequest(
                           from_account_id: Int,
                           to_account_id: Int,
                           room_id: Int,
                           message_id: String,
                           body: String,
                           send_time: Int,
                           update_time: Int,
                         ) {

  def mentionCommand: MentionCommand = MentionCommand(
    FromAccountId(from_account_id),
    RoomId(room_id),
    MessageId(message_id),
    MessageBody(body),
    SendTime(send_time),
    UpdateTime(update_time),
  )


}
