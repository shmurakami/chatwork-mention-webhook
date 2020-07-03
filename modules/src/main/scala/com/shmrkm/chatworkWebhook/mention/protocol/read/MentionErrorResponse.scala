package com.shmrkm.chatworkWebhook.mention.protocol.read

object MentionErrorResponse {

  case class InvalidRequest(success: Boolean = false, error: String = "Invalid Request")

}
