package com.shmrkm.webhook.mention.chatwork

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import com.shmrkm.webhook.mention.chatwork.controller.WebhookController

class Routes(
              webhookController: WebhookController
            ) {
  def routes: Route = {
    path("hello") {
      get {
        webhookController.execute
      }
    }
  }

}
