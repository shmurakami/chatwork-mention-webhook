package com.shmrkm.chatworkWebhook.interface.adaptor

import com.shmrkm.chatworkMention.accessToken.AccessTokenGenerator
import com.shmrkm.chatworkMention.repository.{AuthenticationRepository, ChatworkApiRepository}
import com.shmrkm.chatworkWebhook.auth.usecase.AuthenticationUseCase
import com.shmrkm.chatworkWebhook.domain.model.account.AccountId
import com.shmrkm.chatworkWebhook.domain.model.chatwork.ApiToken
import com.shmrkm.chatworkWebhook.mention.protocol.command.AuthenticationCommand

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class AuthenticationServiceImpl(
    accessTokenGenerator: AccessTokenGenerator,
    chatworkApiRepository: ChatworkApiRepository,
    authenticationRepository: AuthenticationRepository
)(implicit val ec: ExecutionContext)
    extends AuthenticationService {

  override def auth(in: AuthenticationRequest): Future[AuthenticationReply] = {
    val useCase = new AuthenticationUseCase(accessTokenGenerator, chatworkApiRepository, authenticationRepository)
    val request = AuthenticationCommand(AccountId(in.accountId), ApiToken(in.token))
    useCase.execute(request).map {
      case Success(accessToken)   => AuthenticationReply(in.accountId, accessToken.value)
      case Failure(ex: Throwable) => throw ex
    }
  }
}
