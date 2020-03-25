package com.shmrkm.chatworkWebhook.mention.controller

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.redis.RedisClient
import com.shmrkm.chatworkMention.repository.{ChatworkApiRepositoryImpl, MentionRepositoryRedisImpl}
import com.shmrkm.chatworkWebhook.mention.protocol.{MentionCommand, WebhookRequest}
import com.webhook.mention.chatwork.protocol.WebhookResponse

import scala.concurrent.{ExecutionContext, Future}

class WebhookController(implicit system: ActorSystem) {
  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.generic.auto._

  private val chatworkApiConfig = system.settings.config.getConfig("chatwork.api")
  private val redisConfig = system.settings.config.getConfig("redis")

  def route: Route =
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
    val chatworkApiRepository = new ChatworkApiRepositoryImpl(apiUrl, token)
    val mentionRepository = new MentionRepositoryRedisImpl(new RedisClient(redisConfig.getString("host"), redisConfig.getInt("port")))

    // TODO do this via stream
    val mentionRepositoryFuture = mentionRepository.resolve(request.toAccountId)
    val chatworkApiRepositoryFuture = chatworkApiRepository.resolveMentionMessage(request.roomId, request.fromAccountId, request.message)

    (for {
      mentionList <- mentionRepositoryFuture
      newMentionMessage <- chatworkApiRepositoryFuture.map(_.get)
    } yield {
      mentionRepository.store(request.toAccountId, mentionList.add(newMentionMessage))
      WebhookResponse()
    }).recover {
      // do something

      case e => WebhookResponse()
    }
  }
}
