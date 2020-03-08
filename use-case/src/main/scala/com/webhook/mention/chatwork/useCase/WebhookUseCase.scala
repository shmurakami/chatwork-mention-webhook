package com.webhook.mention.chatwork.useCase

import scala.concurrent.{ExecutionContext, Future}

case class Response()

case class WebhookUseCase()(implicit ec: ExecutionContext) {
  def execute(request: Any): Future[Response] = {
    Future.successful(Response())
  }

}
