package com.shmrkm.chatworkWebhook.mention.controller

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{ HttpResponse, StatusCodes }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.shmrkm.chatworkMention.repository.{
  AuthenticationRepository,
  AuthenticationRepositoryFactory,
  ChatworkApiClientFactory,
  ChatworkApiRepository
}
import com.shmrkm.chatworkWebhook.auth.usecase.AuthenticationUseCase
import com.shmrkm.chatworkWebhook.mention.protocol.command.{
  AuthenticationCommand,
  AuthenticationRequest,
  AuthenticationResponse,
  FailureAuthenticationResponse,
  SuccessAuthenticationResponse,
  UnauthenticatedResponse
}
import com.typesafe.scalalogging.Logger

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

class AuthenticationController(implicit val system: ActorSystem)
    extends Controller
    with AuthenticationRepositoryFactory
    with ChatworkApiClientFactory {
  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.generic.auto._
  import io.circe.syntax._

  implicit val ec: ExecutionContext = system.dispatcher

  val logger = Logger(classOf[AuthenticationController])

  val config = system.settings.config

  val authenticationRepository: AuthenticationRepository = factoryAuthenticationRepository()

  val chatworkApiRepository: ChatworkApiRepository = factoryChatworkApiClient()

  def route: Route =
    post {
      extractExecutionContext { implicit ec =>
        entity(as[AuthenticationRequest]) { request =>
          onComplete(execute(request.command)) {
            case Success(response) => complete(response)
            case Failure(ex) =>
              logger.warn(ex.toString)
              complete(
                HttpResponse(
                  entity = UnauthenticatedResponse().asJson.noSpaces,
                  status = StatusCodes.BadRequest
                )
              )
          }
        }
      }
    }

  def execute(request: AuthenticationCommand): Future[AuthenticationResponse] = {
    val useCase = new AuthenticationUseCase(chatworkApiRepository, authenticationRepository)
    useCase.execute(request).map {
      case Success(accessToken)   => SuccessAuthenticationResponse(account_id = request.account_id, token = accessToken)
      case Failure(ex: Throwable) => FailureAuthenticationResponse(ex.getMessage)
    }
  }
}
