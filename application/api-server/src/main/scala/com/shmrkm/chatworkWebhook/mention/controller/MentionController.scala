package com.shmrkm.chatworkWebhook.mention.controller

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.shmrkm.chatworkMention.repository.{AuthenticationRepository, AuthenticationRepositoryFactory}
import com.shmrkm.chatworkWebhook.domain.model.account.AccountId
import com.shmrkm.chatworkWebhook.mention.directive.TokenAuthorizationDirective
import com.shmrkm.chatworkWebhook.mention.protocol.query
import com.shmrkm.chatworkWebhook.mention.usecase.MentionListUseCase
import com.typesafe.scalalogging.Logger

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class MentionController(mentionListUseCase: MentionListUseCase)(implicit system: ActorSystem)
    extends Controller
    with TokenAuthorizationDirective
    with AuthenticationRepositoryFactory {

  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

  override implicit def ec: ExecutionContext = system.dispatcher
  implicit def authRepository: AuthenticationRepository = factoryAuthenticationRepository()
  private val logger                                   = Logger(classOf[MentionController])

  def route: Route =
    get {
      extractExecutionContext { implicit ec =>
        parameters('account_id.as[Int]) { accountId =>
          tokenAuthorization(AccountId(accountId)) { futureAuthentication =>
            onComplete(futureAuthentication) {
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
