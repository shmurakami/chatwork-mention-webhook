package com.shmrkm.chatworkWebhook.mention.controller

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.shmrkm.chatworkMention.repository.{ChatworkApiRepository, MentionRepository}
import com.shmrkm.chatworkWebhook.mention.message.{MessageActor, MessageApplication, MessageCreated}
import com.shmrkm.chatworkWebhook.mention.sample.{SampleActor, SampleRequest}

import scala.concurrent.{ExecutionContext, Future}

class MentionController()(implicit system: ActorSystem) {

  // don't want to put resource to api-server
  private val chatworkApiConfig = system.settings.config.getConfig("chatwork.api")

  def routes: Route =
    extractExecutionContext { implicit ec =>
      parameters('key.?) { request =>
        onSuccess(execute(request)) { response =>
          complete(HttpEntity(ContentTypes.`application/json`, s"""{"value": "${response}"}"""))
        }
      }
    }

  def execute(request: Option[String])(implicit ec: ExecutionContext): Future[String] = {
    // store to queue

    val apiUrl = chatworkApiConfig.getString("url")
    val token = chatworkApiConfig.getString("token")
    val mentionRepository = new MentionRepository()
    val messageApplication = new MessageApplication(new ChatworkApiRepository(apiUrl, token))
    system.actorOf(MessageActor.props(messageApplication)) ! MessageCreated()
    Future.successful(token)
  }

}
