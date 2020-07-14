package com.shmrkm.chatworkWebhook.mention.protocol.query

object MentionErrorResponse {

  case class InvalidRequest(success: Boolean = false, error: String = "Invalid Request")

}
