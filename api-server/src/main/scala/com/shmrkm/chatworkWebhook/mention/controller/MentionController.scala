package com.shmrkm.chatworkWebhook.mention.controller

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.shmrkm.chatworkWebhook.mention.sample.{SampleActor, SampleRequest}

import scala.concurrent.Future

class MentionController()(implicit system: ActorSystem) {

  def routes: Route =
    extractExecutionContext { implicit ec =>
      parameters('key.?) { request =>
        onSuccess(execute(request)) { response =>
          complete(HttpEntity(ContentTypes.`application/json`, s"""{"value": "${response}"}"""))
        }
      }
    }

  def execute(request: Option[String]): Future[String] = {
    system.actorOf(SampleActor.props) ! SampleRequest
    Future.successful("response")
  }

}
