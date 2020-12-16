package com.shmrkm.chatworkWebhook.auth.usecase

import com.shmrkm.chatworkWebhook.domain.model.auth.AccessToken
import com.shmrkm.chatworkWebhook.mention.protocol.command.AuthenticationCommand

import scala.concurrent.Future
import scala.util.Try

trait AuthenticationUseCase {

  def execute(request: AuthenticationCommand): Future[Try[AccessToken]]

}
