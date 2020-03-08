package com.webhook.mention.chatwork.protocol

import com.webhook.mention.chatwork.useCase.Response

object WebhookResponse {
  def apply(): WebhookResponse = {
    ???
  }
}

case class WebhookResponse(value: Any) {
  def asJson(value: Response): String = ???
}
