package com.shmrkm.chatworkWebhook.mention.controller

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._

import scala.concurrent.Future

class MentionController {

  def routes: Route =
    extractExecutionContext { implicit ec =>
      parameters('key.?) { request =>
        onSuccess(execute(request)) { response =>
          complete(HttpEntity(ContentTypes.`application/json`, s"""{"value": "${response}"}"""))
        }
      }
    }

  def execute(request: Option[String]): Future[String] = {
    Future.successful("response")
  }

}
