package com.shmrkm.chatworkWebhook.domain.model.auth

case class AccessToken(value: String)

object AccessToken {
  def generate: AccessToken = ???
}
