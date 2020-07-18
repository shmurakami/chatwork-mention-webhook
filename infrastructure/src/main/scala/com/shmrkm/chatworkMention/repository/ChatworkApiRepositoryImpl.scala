package com.shmrkm.chatworkMention.repository

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.unmarshalling.Unmarshal
import com.shmrkm.chatworkMention.exception.chatworkApi.{ ResponseException, UnexpectedException }
import com.shmrkm.chatworkMention.exception.{ InvalidAccountIdException, RequestFailureException }
import com.shmrkm.chatworkWebhook.domain.model.account._
import com.shmrkm.chatworkWebhook.domain.model.chatwork.{ ApiToken, Me }
import com.shmrkm.chatworkWebhook.domain.model.room.{ Room, RoomIconUrl, RoomId, RoomName }
import com.typesafe.scalalogging.Logger
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

import scala.collection.immutable
import scala.concurrent.{ ExecutionContext, Future }

class ChatworkApiRepositoryImpl(url: String)(
    implicit system: ActorSystem,
    implicit val ec: ExecutionContext
) extends ChatworkApiRepository {

  val logger = Logger(classOf[ChatworkApiRepository])

  def retrieveRoom(roomId: RoomId)(implicit apiToken: ApiToken): Future[Room] = {
    val roomUrl = s"${url}/rooms/${roomId.value}"
    Http()
      .singleRequest(request(roomUrl, apiToken))
      .flatMap {
        case HttpResponse(_: StatusCodes.Success, _, entity, _) => Unmarshal(entity).to[RoomResponse]
        case HttpResponse(_: StatusCodes.ServerError, _, _, _)  => throw ResponseException("")
        case _                                                  => throw UnexpectedException() // it should handle response body
      }
      .map(response => Room(roomId, RoomName(response.name), RoomIconUrl(response.icon_path)))
  }

  def retrieveAccount(roomId: RoomId, fromAccountId: AccountId)(implicit apiToken: ApiToken): Future[FromAccount] = {
    val memberUrl = s"${url}/rooms/${roomId.value}/members"
    Http()
      .singleRequest(request(memberUrl, apiToken))
      .flatMap {
        case HttpResponse(_: StatusCodes.Success, _, entity, _) => Unmarshal(entity).to[List[MemberResponseItem]]
        case HttpResponse(_: StatusCodes.ServerError, _, _, _)  => throw ResponseException("")
        case _                                                  => throw UnexpectedException() // it should handle response body
      }
      .map(MemberResponse(_))
      .map(response =>
        response
          .filterBy(fromAccountId)
          .map(response =>
            FromAccount(fromAccountId, AccountName(response.name), FromAccountAvatarUrl(response.avatar_image_url))
          )
          .head
      )
  }

  private def request(url: String, apiToken: ApiToken): HttpRequest = HttpRequest(
    HttpMethods.GET,
    Uri(url),
    immutable.Seq(
      RawHeader("X-ChatworkToken", apiToken.value)
    )
  )

  override def resolveAccount(accountId: AccountId)(implicit apiToken: ApiToken): Future[MeResponse] = {
    val meUrl = s"${url}/me"
    Http()
      .singleRequest(request(meUrl, apiToken))
      .flatMap(Unmarshal(_).to[MeResponse])
      .map(response => {
        if (response.account_id == accountId.value) response
        else {
          logger.warn(s"detect account id mismatching request ${accountId.value} and token ${response.account_id}")
          throw InvalidAccountIdException(s"invalid account id ${accountId.value}")
        }
      })
      .recover {
        case e =>
          logger.warn(s"failed to request to chatwork api, $e")
          throw RequestFailureException("failed to request to chatwork api")
      }
  }

  override def me(implicit apiToken: ApiToken): Future[Me] = {
    val meUrl = s"${url}/me"
    Http()
      .singleRequest(request(meUrl, apiToken))
      .flatMap(Unmarshal(_).to[MeResponse])
      .map { meResponse => Me(accountId = AccountId(meResponse.account_id), name = AccountName(meResponse.name)) }
  }
}
