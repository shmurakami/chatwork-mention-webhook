package com.shmrkm.webhook.mention.chatwork.controller

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.webhook.mention.chatwork.protocol.{WebhookRequest, WebhookResponse}
import com.webhook.mention.chatwork.useCase.WebhookUseCase

import scala.concurrent.Future

class WebhookController(useCase: WebhookUseCase) {

  def execute: Route =
    extractExecutionContext { implicit ec =>
      parameters('value.as[String]).as(WebhookRequest) { request =>
        val result = Future.successful(request.value)
        onSuccess(result) { response =>
          complete(HttpEntity(ContentTypes.`application/json`, s"""{"success":true, "value": "${response}"}"""))
//          complete(WebhookResponse().asJson(result))
        }
      }
    }
}
