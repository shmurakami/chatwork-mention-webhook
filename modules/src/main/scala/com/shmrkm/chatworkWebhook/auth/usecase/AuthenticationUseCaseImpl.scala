package com.shmrkm.chatworkWebhook.auth.usecase

import com.shmrkm.chatworkMention.accessToken.AccessTokenGenerator
import com.shmrkm.chatworkMention.repository.{AuthenticationRepository, ChatworkApiRepository}
import com.shmrkm.chatworkWebhook.domain.model.auth.{AccessToken, Authentication}
import com.shmrkm.chatworkWebhook.mention.protocol.command.AuthenticationCommand

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class AuthenticationUseCaseImpl(
    accessTokenGenerator: AccessTokenGenerator,
    chatworkApiRepository: ChatworkApiRepository,
    authenticationRepository: AuthenticationRepository
)(implicit ec: ExecutionContext) extends AuthenticationUseCase {

  def execute(request: AuthenticationCommand): Future[Try[AccessToken]] = {
    chatworkApiRepository
      .me(request.token)
      .filter(_.accountId == request.account_id)
      .flatMap { _ => authenticationRepository.issueAccessToken(Authentication(request.account_id, request.token, accessTokenGenerator.generate(request.account_id))) }
  }

}
