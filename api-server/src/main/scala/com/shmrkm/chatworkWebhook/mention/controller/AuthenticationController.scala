package com.shmrkm.chatworkWebhook.mention.controller

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{ContentTypes, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.redis.RedisClient
import com.shmrkm.chatworkMention.repository.{AuthenticationRepository, AuthenticationRepositoryImpl, ChatworkApiRepositoryImpl}
import com.shmrkm.chatworkWebhook.mention.protocol.command.{AuthenticationRequest, AuthenticationResponse, UnauthenticatedResponse}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class AuthenticationController(implicit system: ActorSystem) extends Controller {
  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.generic.auto._
  import io.circe.syntax._

  implicit val ec: ExecutionContext = system.dispatcher

  val config = system.settings.config

  val authenticationRepository: AuthenticationRepository = new AuthenticationRepositoryImpl(
    new RedisClient(config.getString("redis.host"), config.getInt("redis.port"))
  )

  def route: Route =
    post {
      extractExecutionContext { implicit ec =>
        entity(as[AuthenticationRequest]) { request =>
          onComplete(execute(request)) {
            case Success(response) => complete(response)
            case Failure(ex) =>
              complete(
                HttpResponse(
                  entity = UnauthenticatedResponse().asJson.toString,
                  status = StatusCodes.BadRequest,
                  headers = Seq(ContentTypes.`application/json`)
                )
              )
          }
        }
      }
    }

  def execute(request: AuthenticationRequest): Future[AuthenticationResponse] = {
    val chatworkApiRepository = new ChatworkApiRepositoryImpl(config.getString("chatwork.api.url"), request.token)
    chatworkApiRepository
      .me()
      .filter(_.accountId == request.account_id)
      .flatMap { _ =>
        authenticationRepository
          .issueAccessToken(request.account_id)
          .map { accessToken => AuthenticationResponse(account_id = request.account_id, token = accessToken) }
      }
  }
}
