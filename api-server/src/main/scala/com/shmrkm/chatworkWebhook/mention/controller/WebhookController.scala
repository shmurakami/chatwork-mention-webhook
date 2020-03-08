package com.shmrkm.chatworkWebhook.mention.controller

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.webhook.mention.chatwork.protocol.{WebhookRequest, WebhookResponse}

import scala.concurrent.Future

class WebhookController() {
  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.generic.auto._

  def execute: Route =
    extractExecutionContext { implicit ec =>
      entity(as[WebhookRequest]) { request =>
        val response: Future[WebhookResponse] = Future.successful(WebhookResponse(request.value))
        onSuccess(response) { res =>
          complete(res)
        }
      }
    }
}
