package com.shmrkm.chatworkWebhook.mention.controller

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.redis.RedisClient
import com.shmrkm.chatworkMention.repository.{ChatworkApiRepository, MentionRepositoryRedisImpl}
import com.shmrkm.chatworkWebhook.domain.model.AccountName
import com.shmrkm.chatworkWebhook.domain.model.account.FromAccountAvatarUrl
import com.shmrkm.chatworkWebhook.domain.model.mention.MentionMessage
import com.shmrkm.chatworkWebhook.domain.model.room.{RoomIconUrl, RoomName}
import com.shmrkm.chatworkWebhook.mention.protocol.{MentionCommand, WebhookRequest}
import com.webhook.mention.chatwork.protocol.WebhookResponse

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class WebhookController(implicit system: ActorSystem) {
  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.generic.auto._

  private val chatworkApiConfig = system.settings.config.getConfig("chatwork.api")
  private val redisConfig = system.settings.config.getConfig("redis")

  def rouete: Route =
    extractExecutionContext { implicit ec =>
      entity(as[WebhookRequest]) { request =>
        onSuccess(execute(request.mentionCommand)) { response =>
          complete(response)
        }
      }
    }

  def execute(request: MentionCommand)(implicit ec: ExecutionContext): Future[WebhookResponse] = {
    // store to queue

    val apiUrl = chatworkApiConfig.getString("url")
    val token = chatworkApiConfig.getString("token")
    val chatworkApiRepository = new ChatworkApiRepository(apiUrl, token)
    val mentionRepository = new MentionRepositoryRedisImpl(new RedisClient(redisConfig.getString("host"), redisConfig.getInt("port")))

//    chatworkApiRepository.resolveAccount(request.roomId, request.messageId).onComplete {
//      case Success(acc: Option[AccountName]) => acc.map {
//        case accountName => WebhookResponse()
//        case _ => // do something
//      }
//      case Failure(_) => // do something
//    }
//
//    Future.successful(Done)

    chatworkApiRepository.resolveAccount(request.roomId, request.messageId).map {
      case maybeAccount => maybeAccount.map {
        case a: AccountName => {
          val mentionMessage = MentionMessage(
            fromAccountId = request.fromAccountId,
            fromAccountAvatarUrl = FromAccountAvatarUrl("#"),
            roomId = request.roomId,
            roomName = RoomName("room name"),
            roomIconUrl = RoomIconUrl("#"),
            messageId = request.messageId,
            body = request.body,
            sendTime = request.sendTime,
            updateTime = request.updateTime,
          )
          mentionRepository.resolve(request.toAccountId).map { list =>
            val newList = list.add(mentionMessage)
            mentionRepository.store(request.toAccountId, newList)
          }
        }
        case _ =>
      }
      case None =>
    }
    // TODO do this via stream
    // TODO retrieve sender account name through chatwork api, save record to redis, request server push to client
    Future.successful(WebhookResponse())
  }
}
