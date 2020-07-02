package com.shmrkm.chatworkWebhook.auth.usecase

import com.shmrkm.chatworkMention.repository.{AuthenticationRepository, ChatworkApiRepository}
import com.shmrkm.chatworkWebhook.domain.model.auth.AccessToken
import com.shmrkm.chatworkWebhook.mention.protocol.command.AuthenticationCommand

import scala.concurrent.{ExecutionContext, Future}

class AuthenticationUseCase(
    chatworkApiRepository: ChatworkApiRepository,
    authenticationRepository: AuthenticationRepository
)(implicit ec: ExecutionContext) {

  def execute(request: AuthenticationCommand): Future[AccessToken] = {
    chatworkApiRepository
      .me()
      .filter(_.accountId == request.account_id)
      .flatMap { _ => authenticationRepository.issueAccessToken(request.account_id) }
  }

}
