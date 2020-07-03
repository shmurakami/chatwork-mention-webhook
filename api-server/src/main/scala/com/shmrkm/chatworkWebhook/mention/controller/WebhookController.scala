package com.shmrkm.chatworkWebhook.mention.controller

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive1, MissingHeaderRejection, Route}
import akka.pattern.ask
import akka.util.Timeout
import com.shmrkm.chatworkWebhook.mention.mention.MentionHandlerActor
import com.shmrkm.chatworkWebhook.mention.mention.MentionHandlerActor.ReceiveMention
import com.shmrkm.chatworkWebhook.mention.protocol.command.WebhookResponse
import com.shmrkm.chatworkWebhook.mention.protocol.write.{MentionCommand, WebhookRequest}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class WebhookController(implicit system: ActorSystem) extends Controller {

  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.generic.auto._

  private val chatworkConfig = system.settings.config.getConfig("chatwork")

  def verifySignature: Directive1[String] = {
    val headerKey       = chatworkConfig.getString("webhook.signature_header_key")
    val expectSignature = chatworkConfig.getString("webhook.signature")

    headerValueByName(headerKey).filter(_ == expectSignature, MissingHeaderRejection(headerKey))
  }

  implicit val timeout: Timeout =
    system.settings.config.getInt("chatwork-mention-webhook.api-server.timeout-seconds") seconds

  def route: Route =
    post {
      verifySignature { _ =>
        extractExecutionContext { implicit ec =>
          entity(as[WebhookRequest]) { request =>
            onComplete(execute(request.mentionCommand)) { response =>
              complete(response)
            }
          }
        }
      }
    }

  /**
    * store to queue
    */
  def execute(request: MentionCommand)(implicit system: ActorSystem, ec: ExecutionContext): Future[WebhookResponse] = {
    system.actorOf(MentionHandlerActor.props).ask(ReceiveMention(request)).map(_ => WebhookResponse())
  }
}
