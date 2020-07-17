package com.shmrkm.chatworkWebhook.mention.controller

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.AuthenticationFailedRejection.{ CredentialsMissing, CredentialsRejected }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ AuthenticationFailedRejection, Route }
import com.shmrkm.chatworkMention.exception.{ InvalidAccountIdException, KeyNotFoundException, RequestFailureException }
import com.shmrkm.chatworkMention.repository.{
  AuthenticationRepository,
  AuthenticationRepositoryFactory,
  MentionRepository,
  MentionRepositoryFactory
}
import com.shmrkm.chatworkWebhook.domain.model.account.AccountId
import com.shmrkm.chatworkWebhook.domain.model.auth.AccessToken
import com.shmrkm.chatworkWebhook.domain.model.mention.MentionList
import com.shmrkm.chatworkWebhook.mention.protocol.query
import com.shmrkm.chatworkWebhook.mention.protocol.query.MentionErrorResponse.InvalidRequest
import com.shmrkm.chatworkWebhook.mention.protocol.query.MentionQuery
import com.typesafe.scalalogging.Logger

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

class MentionController(implicit system: ActorSystem)
    extends Controller
    with AuthenticationRepositoryFactory
    with MentionRepositoryFactory {

  override implicit def ec: ExecutionContext = system.dispatcher

  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.generic.auto._

  val mentionRepository: MentionRepository = factoryMentionRepository()

  val authRepository: AuthenticationRepository = factoryAuthenticationRepository()

  val logger = Logger(classOf[MentionController])

  //  implicit def rejectionHandler =
//    RejectionHandler
//      .newBuilder()
//      .handle {
//        case AuthenticationFailedRejection(cause, challenge) =>
//          complete((StatusCodes.Unauthorized, "Invalid Request"))
//      }
//      .result()

//  def verifyAccessToken(accountId: AccountId): Directive1[ApiToken] = {
//    headerValueByName("X-Token") { token =>
//      authRepository.resolve(AccessToken(token)) match {
//        case Some(authentication) if (authentication.accountId == accountId) => extract()
//        case None                                                            => reject(AuthenticationFailedRejection(CredentialsRejected, null))
//      }
//    }
//  }

  def route: Route =
    get {
      extractExecutionContext { implicit ec =>
        parameters('account_id.as[Int]) { accountId =>
          // TODO FIXME validation
          headerValueByName("X-Token") { token =>
            onComplete(authRepository.resolve(AccessToken(token))) {
              case Failure(ex) =>
                logger.warn(s"failure to resolve authentication $ex")
                complete("unexpected error occurred")
              case Success(maybeAuthentication) =>
                maybeAuthentication match {
                  case Right(authentication) if authentication.accountId.value == accountId => {
                    onSuccess(execute(query.MentionQuery(AccountId(accountId)))) {
                      // Either? Try?
                      case Right(mentionList) => complete(mentionList)
                      case Left(_)            => complete(StatusCodes.BadRequest, InvalidRequest())
                    }
                  }
                  case Left(ex: KeyNotFoundException) => reject(AuthenticationFailedRejection(CredentialsMissing, null))
                  case _                              => reject(AuthenticationFailedRejection(CredentialsRejected, null))
                }
            }
          }
        }
      }
    }

  def execute(query: MentionQuery): Future[Either[String, MentionList]] = {
    // seems Either Left should be any type

    mentionRepository
      .resolve(query.accountId)
      .map(Right(_))
      .recover {
        case e: InvalidAccountIdException =>
          logger.warn(e.toString)
          Left("error")
        case e: RequestFailureException =>
          logger.warn(e.toString)
          Left("try again")
      }
  }

}
