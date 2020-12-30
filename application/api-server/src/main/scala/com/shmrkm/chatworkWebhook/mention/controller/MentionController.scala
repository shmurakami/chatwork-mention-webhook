package com.shmrkm.chatworkWebhook.mention.controller

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.shmrkm.chatworkMention.repository.{AuthenticationRepository, AuthenticationRepositoryFactory}
import com.shmrkm.chatworkWebhook.auth.exception.AuthenticationFailureException
import com.shmrkm.chatworkWebhook.domain.model.account.AccountId
import com.shmrkm.chatworkWebhook.domain.model.auth.Authentication
import com.shmrkm.chatworkWebhook.mention.protocol.query
import com.shmrkm.chatworkWebhook.mention.usecase.MentionListUseCase
import com.typesafe.scalalogging.Logger

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class MentionController(mentionListUseCase: MentionListUseCase)(implicit system: ActorSystem)
    extends Controller
    with AuthenticationRepositoryFactory {

  override implicit def ec: ExecutionContext = system.dispatcher

  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

  private val authRepository: AuthenticationRepository = factoryAuthenticationRepository()
  private val logger                                   = Logger(classOf[MentionController])

  def verifyAccessToken(requestToken: String, accountId: AccountId): Future[Authentication] = {
    authRepository.resolve(accountId).flatMap {
      case Right(authentication) if authentication.accountId == accountId && authentication.accessToken.value == requestToken => Future.successful(authentication)
      case Right(_) => Future.failed(AuthenticationFailureException())
      case Left(ex: Throwable) => Future.failed(AuthenticationFailureException())
    }
  }

  def route: Route =
    get {
      extractExecutionContext { implicit ec =>
        parameters('account_id.as[Int]) { accountId =>
          headerValueByName("X-Token") { token =>
            onComplete(verifyAccessToken(token, AccountId(accountId))) {
              case Failure(ex) =>
                logger.warn(s"failure to resolve authentication $ex")
                complete("""{"error": "unauthorized}""")
              case Success(_) =>
                onSuccess(mentionListUseCase.execute(query.MentionQuery(AccountId(accountId)))) {
                  case Right(mentionList) => complete(mentionList)
                  case Left(_)            => complete(StatusCodes.BadRequest)
                }
            }
          }
        }
      }
    }
}
