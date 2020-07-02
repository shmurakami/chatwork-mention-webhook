package com.shmrkm.chatworkWebhook.mention

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.shmrkm.chatworkWebhook.mention.controller.{AuthenticationController, MentionController, WebhookController}

class Routes(
    authenticationController: AuthenticationController,
    webhookController: WebhookController,
    mentionController: MentionController
) {

  def routes: Route = {
    pathEndOrSingleSlash {
      complete(HttpResponse(entity = """{}"""))
    } ~ path("auth") {
      authenticationController.route
    } ~ path("list") {
      mentionController.route
    } ~ path("mention-webhook") {
      webhookController.route
    }
  }

}
