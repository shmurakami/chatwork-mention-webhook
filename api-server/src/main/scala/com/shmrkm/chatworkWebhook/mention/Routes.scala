package com.shmrkm.chatworkWebhook.mention

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.shmrkm.chatworkWebhook.mention.controller.{MentionController, WebhookController}

class Routes(
              webhookController: WebhookController,
              mentionController: MentionController
            ) {
  def routes: Route = {
    path("list") {
      get {
        mentionController.routes
      }
    } ~ path("mention-webhook") {
      post {
        webhookController.rouete
      }
    }
  }

}
