package com.shmrkm.chatworkWebhook.interface.adaptor

import com.shmrkm.chatworkWebhook.auth.usecase.AuthenticationUseCase
import com.shmrkm.chatworkWebhook.domain.model.auth.AccessToken
import com.shmrkm.chatworkWebhook.mention.protocol.command.AuthenticationCommand
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

class AuthenticationServiceImplSpec() extends AnyWordSpecLike with Matchers with ScalaFutures {

  "authentication service" should {
    implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

    "return valid reply for valid request" in {
      val authenticationUseCase: AuthenticationUseCase = (request: AuthenticationCommand) =>
        Future {
          Success(AccessToken("valid"))
        }
      val authenticationService = new AuthenticationServiceImpl(authenticationUseCase)
      val reply                 = authenticationService.auth(AuthenticationRequest(accountId = 1, token = "token")).futureValue

      reply shouldBe AuthenticationReply(accountId = 1, token = "valid")
    }

    "throw exception if fail to authenticate" in {
      val authenticationUseCase: AuthenticationUseCase =
        (request: AuthenticationCommand) => Future.failed(new Exception())
      val authenticationService = new AuthenticationServiceImpl(authenticationUseCase)

      intercept[Exception] {
        authenticationService
          .auth(AuthenticationRequest(accountId = 1, token = "token"))
          .futureValue
      }
    }
  }

}
