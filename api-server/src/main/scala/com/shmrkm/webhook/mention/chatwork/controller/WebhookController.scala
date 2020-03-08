package com.shmrkm.webhook.mention.chatwork.controller

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.webhook.mention.chatwork.protocol.{WebhookRequest, WebhookResponse}

import scala.concurrent.Future

class WebhookController() {

  def execute: Route =
    extractExecutionContext { implicit ec =>
      parameters('value.as[String]).as(WebhookRequest) { request =>
        val response: Future[WebhookResponse] = Future.successful(WebhookResponse(request.value))
        onSuccess(response) { res =>
          complete(HttpEntity(ContentTypes.`application/json`, res.json))
        }
      }
    }
}
