package com.shmrkm.webhook.mention.chatwork

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import com.shmrkm.webhook.mention.chatwork.controller.WebhookController

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
