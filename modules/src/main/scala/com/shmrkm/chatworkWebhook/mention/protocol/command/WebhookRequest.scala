package com.shmrkm.chatworkWebhook.mention.protocol.command

case class WebhookRequest(
    webhook_setting_id: String,
    webhook_event_type: String,
    webhook_event_time: Long,
    webhook_event: WebhookEvent
) {
  def mentionCommand: MentionCommand = webhook_event.mentionCommand
}
