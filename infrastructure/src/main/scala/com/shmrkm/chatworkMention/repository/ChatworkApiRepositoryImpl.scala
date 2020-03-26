package com.shmrkm.chatworkMention.repository

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.unmarshalling.Unmarshal
import com.shmrkm.chatworkWebhook.domain.model.account.{FromAccount, FromAccountAvatarUrl, FromAccountId, ToAccountId}
import com.shmrkm.chatworkWebhook.domain.model.mention.MentionMessage
import com.shmrkm.chatworkWebhook.domain.model.message.Message
import com.shmrkm.chatworkWebhook.domain.model.room.{Room, RoomIconUrl, RoomId, RoomName}
import com.typesafe.scalalogging.Logger
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

// TODO make token model
class ChatworkApiRepositoryImpl(url: String, token: String)(implicit system: ActorSystem, implicit val ec: ExecutionContext)
  extends ChatworkApiRepository {
  override def resolveMentionMessage(roomId: RoomId, fromAccountId: FromAccountId, message: Message): Future[Option[MentionMessage]] = {
    // account name, avatar image url, from rooms/:id/members
    // room name, room icon url, from rooms/:id

    for {
      fromAccount <- retrieveAccount(roomId, fromAccountId)
      room <- retrieveRoom(roomId)
    } yield {
      Some(MentionMessage(
        fromAccount.id,
        fromAccount.avatar,
        room.id,
        room.name,
        room.iconUrl,
        message.id,
        message.body,
        message.sendTime,
        message.updateTime,
      ))
    }
  }

  private def retrieveRoom(roomId: RoomId): Future[Room] = {
    val roomUrl = s"${url}/rooms/${roomId.value}"
    Http().singleRequest(request(roomUrl))
      .flatMap(Unmarshal(_).to[RoomResponse])
      .map(response => Room(roomId, RoomName(response.name), RoomIconUrl(response.icon_path)))
  }

  private def retrieveAccount(roomId: RoomId, fromAccountId: FromAccountId): Future[FromAccount] = {
    val memberUrl = s"${url}/rooms/${roomId.value}/members"
    Http().singleRequest(request(memberUrl))
      .flatMap(Unmarshal(_).to[List[MemberResponseItem]])
      .map(MemberResponse(_))
      .map(response => response.filterBy(fromAccountId)
        .map(response => FromAccount(fromAccountId, FromAccountAvatarUrl(response.avatar_image_url)))
        .head)
  }

  private def request(url: String): HttpRequest = HttpRequest(
    HttpMethods.GET,
    Uri(url),
    immutable.Seq(
      RawHeader("X-ChatworkToken", token),
    )
  )

  override def resolveAccount(accountId: ToAccountId): Future[Option[MeResponse]] = {
    val meUrl = s"${url}/me"
    Http().singleRequest(request(meUrl))
      .flatMap(Unmarshal(_).to[MeResponse])
      .map(response => {
        if (response.account_id == accountId.value) {
          Some(response)
        } else {
          Logger(classOf[ChatworkApiRepository]).warn(s"detect account id mismatching request ${accountId.value} and token ${response.account_id}")
          None
        }
      })
  }
}
