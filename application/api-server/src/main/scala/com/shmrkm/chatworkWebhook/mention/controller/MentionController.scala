package com.shmrkm.chatworkWebhook.mention.controller

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.shmrkm.chatworkMention.exception.{ InvalidAccountIdException, RequestFailureException }
import com.shmrkm.chatworkMention.repository.{
  AuthenticationRepository,
  AuthenticationRepositoryFactory,
  MentionRepository,
  MentionRepositoryFactory
}
import com.shmrkm.chatworkWebhook.domain.model.account.AccountId
import com.shmrkm.chatworkWebhook.domain.model.auth.{ AccessToken, Authentication }
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

  private val mentionRepository: MentionRepository     = factoryMentionRepository()
  private val authRepository: AuthenticationRepository = factoryAuthenticationRepository()
  private val logger                                   = Logger(classOf[MentionController])

  def verifyAccessToken(requestToken: String, accountId: AccountId): Future[Authentication] = {
    authRepository.resolve(AccessToken(requestToken)).map {
      case Right(authentication) if authentication.accountId == accountId => authentication
//      case Left(ex: KeyNotFoundException) => Future.failed(ex)
//      case _ => ???
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
                complete("""{"error": "unexpected error occurred"}""")
              case Success(_) =>
                onSuccess(execute(query.MentionQuery(AccountId(accountId)))) {
                  case Right(mentionList) => complete(mentionList)
                  case Left(_)            => complete(StatusCodes.BadRequest, InvalidRequest())
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
