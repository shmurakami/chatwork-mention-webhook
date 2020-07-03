package com.shmrkm.chatworkWebhook.mention.controller

import akka.http.scaladsl.server.Route

trait Controller {

  def route: Route

}
