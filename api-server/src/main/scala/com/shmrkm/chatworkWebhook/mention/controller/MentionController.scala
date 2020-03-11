package com.shmrkm.chatworkWebhook.mention.controller

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

import scala.concurrent.{ExecutionContext, Future}

class MentionController(implicit system: ActorSystem) {

  // don't want to put resource to api-server
  private val redisConfig = system.settings.config.getConfig("redis")

  def routes: Route =
    extractExecutionContext { implicit ec =>
      parameters('key.?) { request =>
        onSuccess(execute(request)) { response =>
          complete(HttpEntity(ContentTypes.`application/json`, s"""{"value": "${response}"}"""))
        }
      }
    }

  def execute(request: Option[String])(implicit ec: ExecutionContext): Future[Boolean] = {
    // store to queue
    Future.successful(true)
  }

}
