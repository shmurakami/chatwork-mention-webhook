package com.shmrkm.chatworkWebhook.mention.directive

import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives._
import com.shmrkm.chatworkMention.repository.AuthenticationRepository
import com.shmrkm.chatworkWebhook.auth.exception.AuthenticationFailureException
import com.shmrkm.chatworkWebhook.domain.model.account.AccountId
import com.shmrkm.chatworkWebhook.domain.model.auth.Authentication

import scala.concurrent.{ExecutionContext, Future}

trait TokenAuthorizationDirective {
  implicit def authRepository: AuthenticationRepository
  implicit def ec: ExecutionContext

  private def verifyAccessToken(requestToken: String, accountId: AccountId): Future[Authentication] = {
    authRepository.resolve(accountId).flatMap {
      case Right(authentication) if authentication.accountId == accountId && authentication.accessToken.value == requestToken => Future.successful(authentication)
      case Right(_) => Future.failed(AuthenticationFailureException())
      case Left(ex: Throwable) => Future.failed(AuthenticationFailureException())
    }
  }

  def tokenAuthorization(accountId: AccountId): Directive1[Future[Authentication]] = {
    headerValueByName("Authorization").map { token =>
      verifyAccessToken(token, accountId)
    }
  }
}
