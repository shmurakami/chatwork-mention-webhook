package com.webhook.mention.chatwork.protocol

case class WebhookResponse(value: String) {
  // TODO use circe
  def json: String = s"""{"success": true, "value": "${value}"}"""
}
