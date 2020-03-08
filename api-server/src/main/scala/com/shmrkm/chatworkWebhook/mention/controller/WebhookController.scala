package com.shmrkm.chatworkWebhook.mention.controller

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.webhook.mention.chatwork.protocol.{MentionCommand, WebhookRequest, WebhookResponse}

import scala.concurrent.Future

class WebhookController() {
  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.generic.auto._

  def execute: Route =
    extractExecutionContext { implicit ec =>
      entity(as[WebhookRequest]) { params =>
        onSuccess(process(params)) { response =>
          complete(response)
        }
      }
    }

  def process(request: WebhookRequest): Future[WebhookResponse] = {
    val command = request.mentionCommand
    // TODO do this via stream
    // TODO retrieve sender account name through chatwork api, save record to redis, request server push to client
    // redis value should be read model
    Future.successful(WebhookResponse("value"))
  }
}
