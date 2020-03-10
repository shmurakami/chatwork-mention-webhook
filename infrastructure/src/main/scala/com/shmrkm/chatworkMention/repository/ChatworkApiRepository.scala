package com.shmrkm.chatworkMention.repository

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.unmarshalling.Unmarshal
import com.shmrkm.chatworkWebhook.domain.model.{AccountName, MessageId, RoomId}

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

case class AccountResponse(account_id: Int, name: String, avatar_image_url: String)

case class ApiResponse(
                        message_id: String,
                        account: AccountResponse,
                        body: String,
                        send_time: Int,
                        update_time: Int,
                      )

class ChatworkApiRepository(url: String, token: String)(implicit system: ActorSystem, implicit val ec: ExecutionContext) {
  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.generic.auto._

  def resolveAccountName(roomId: RoomId, messageId: MessageId): Future[AccountName] = {
    val messageUrl = s"${url}/rooms/${roomId.value}/messages/${messageId.value}"
    val request = HttpRequest(
      HttpMethods.GET,
      Uri(messageUrl),
      immutable.Seq(
        RawHeader("X-ChatworkToken", token),
      )
    )

    // TODO resolve room name

    val response = Http().singleRequest(request).flatMap(Unmarshal(_).to[ApiResponse])
    val accountName = response.map(apiResponse => AccountName(apiResponse.account.name))
    accountName
  }
}
