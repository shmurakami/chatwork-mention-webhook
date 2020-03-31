package com.shmrkm.chatworkWebhook.mention.controller

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive1, MissingHeaderRejection, Route}
import com.redis.RedisClient
import com.shmrkm.chatworkMention.repository.{ChatworkApiRepositoryImpl, MentionRepositoryRedisImpl}
import com.shmrkm.chatworkWebhook.mention.protocol.write.{MentionCommand, WebhookRequest}
import com.typesafe.scalalogging.Logger
import com.webhook.mention.chatwork.protocol.WebhookResponse

import scala.concurrent.{ExecutionContext, Future}

class WebhookController(implicit system: ActorSystem) {

  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.generic.auto._

  private val chatworkConfig = system.settings.config.getConfig("chatwork")
  private val redisConfig    = system.settings.config.getConfig("redis")

  private val logger = Logger(classOf[WebhookController])

  def verifySignature: Directive1[String] = {
    val headerKey       = chatworkConfig.getString("webhook.signature_header_key")
    val expectSignature = chatworkConfig.getString("webhook.signature")

    headerValueByName(headerKey).filter(_ == expectSignature, MissingHeaderRejection(headerKey))
  }

  def route: Route =
    verifySignature { _ =>
      extractExecutionContext { implicit ec =>
        entity(as[WebhookRequest]) { request =>
          onSuccess(execute(request.mentionCommand)) { response => complete(response) }
        }
      }
    }

  def execute(request: MentionCommand)(implicit ec: ExecutionContext): Future[WebhookResponse] = {
    // store to queue

    val apiUrl                = chatworkConfig.getString("api.url")
    val token                 = chatworkConfig.getString("api.token")
    val chatworkApiRepository = new ChatworkApiRepositoryImpl(apiUrl, token)
    val mentionRepository = new MentionRepositoryRedisImpl(
      new RedisClient(redisConfig.getString("host"), redisConfig.getInt("port"))
    )

    // TODO do this via stream
    val mentionRepositoryFuture = mentionRepository.resolve(request.toAccountId)
    val chatworkApiRepositoryFuture =
      chatworkApiRepository.resolveMentionMessage(request.roomId, request.fromAccountId, request.message)

    (for {
      mentionList       <- mentionRepositoryFuture
      newMentionMessage <- chatworkApiRepositoryFuture.map(_.get)
    } yield {
      mentionRepository.store(request.toAccountId, mentionList.add(newMentionMessage))
      WebhookResponse()
    }).recover {
      case e =>
        logger.warn(s"failed to proceed mention request due to $e")
        WebhookResponse()
    }
  }
}
