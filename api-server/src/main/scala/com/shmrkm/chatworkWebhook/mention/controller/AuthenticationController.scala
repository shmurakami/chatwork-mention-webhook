package com.shmrkm.chatworkWebhook.mention.controller

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{complete, extractExecutionContext, headerValueByName, onSuccess, parameters}
import akka.http.scaladsl.server.Route
import com.shmrkm.chatworkWebhook.domain.model.account.AccountId
import com.shmrkm.chatworkWebhook.mention.protocol.read.MentionErrorResponse.InvalidRequest
import com.shmrkm.chatworkWebhook.mention.protocol.read.MentionQuery

class AuthenticationController(implicit system: ActorSystem) extends Controller {

  def route: Route =
    extractExecutionContext { implicit ec =>
      ???
    }


}
