package com.shmrkm.chatworkWebhook.mention.protocol.write

import com.shmrkm.chatworkWebhook.domain.model.account.AccountId
import com.shmrkm.chatworkWebhook.domain.model.message.{ MessageBody, MessageId, SendTime, UpdateTime }
import com.shmrkm.chatworkWebhook.domain.model.room.RoomId

case class WebhookRequest(
    webhook_setting_id: String,
    webhook_event_type: String,
    webhook_event_time: Long,
    webhook_event: WebhookEvent
) {
  def mentionCommand: MentionCommand = webhook_event.mentionCommand
}

case class WebhookEvent(
    from_account_id: Int,
    to_account_id: Int,
    room_id: Int,
    message_id: String,
    body: String,
    send_time: Int,
    update_time: Int
) {

  def mentionCommand: MentionCommand = MentionCommand(
    AccountId(from_account_id),
    AccountId(to_account_id),
    RoomId(room_id),
    MessageId(message_id),
    MessageBody(body),
    SendTime(send_time),
    UpdateTime(update_time)
  )

}
