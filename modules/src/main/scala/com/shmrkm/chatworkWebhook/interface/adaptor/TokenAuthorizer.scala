package com.shmrkm.chatworkWebhook.interface.adaptor

import com.shmrkm.chatworkMention.repository.AuthenticationRepository
import com.shmrkm.chatworkWebhook.auth.exception.AuthenticationFailureException
import com.shmrkm.chatworkWebhook.domain.model.account.AccountId
import com.shmrkm.chatworkWebhook.domain.model.auth.{AccessToken, Authentication}

import scala.concurrent.{ExecutionContext, Future}

trait TokenAuthorizer {
  implicit def authenticationRepository: AuthenticationRepository

  def authorize(accountId: AccountId, token: Option[AccessToken])(implicit ec: ExecutionContext): Future[Either[Throwable, Authentication]] = {
    token match {
      case None => Future { Left(AuthenticationFailureException()) }
      case Some(token) => {
        authenticationRepository.resolve(accountId).map {
          case Right(authentication)
              if authentication.accountId == accountId && authentication.accessToken.value == token.value =>
            Right(authentication)
          case Right(_)            => Left(AuthenticationFailureException())
          case Left(ex: Throwable) => Left(AuthenticationFailureException())
        }
      }
    }
  }

}
