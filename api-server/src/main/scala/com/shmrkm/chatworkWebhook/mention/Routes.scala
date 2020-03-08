package com.shmrkm.chatworkWebhook.mention

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.shmrkm.chatworkWebhook.mention.controller.WebhookController

class Routes(
              webhookController: WebhookController
            ) {
  def routes: Route = {
    path("list") {
      get {
        ???
      }
    } ~ path("mention") {
      post {
        webhookController.execute
      }
    }
  }

}
