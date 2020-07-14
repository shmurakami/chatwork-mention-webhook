package com.shmrkm.chatworkWebhook.mention.controller

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.shmrkm.chatworkMention.repository.{AuthenticationRepository, AuthenticationRepositoryFactory, ChatworkApiRepositoryImpl}
import com.shmrkm.chatworkWebhook.auth.usecase.AuthenticationUseCase
import com.shmrkm.chatworkWebhook.mention.protocol.command.{AuthenticationCommand, AuthenticationRequest, AuthenticationResponse, UnauthenticatedResponse}
import com.typesafe.scalalogging.Logger

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class AuthenticationController(implicit system: ActorSystem) extends Controller with AuthenticationRepositoryFactory {
  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.generic.auto._
  import io.circe.syntax._

  implicit val ec: ExecutionContext = system.dispatcher

  val logger = Logger(classOf[AuthenticationController])

  val config = system.settings.config

  val authenticationRepository: AuthenticationRepository = factoryAuthenticationRepository()

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
                  entity = UnauthenticatedResponse().asJson.toString,
                  status = StatusCodes.BadRequest
                )
              )
          }
        }
      }
    }

  def execute(request: AuthenticationCommand): Future[AuthenticationResponse] = {
    val chatworkApiRepository = new ChatworkApiRepositoryImpl(config.getString("chatwork.api.url"), request.token)
    val useCase               = new AuthenticationUseCase(chatworkApiRepository, authenticationRepository)
    useCase.execute(request).map { accessToken =>
      AuthenticationResponse(account_id = request.account_id, token = accessToken)
    }
  }
}
