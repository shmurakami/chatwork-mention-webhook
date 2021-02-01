package com.shmrkm.chatworkWebhook.interface.adaptor

import akka.grpc.scaladsl.Metadata
import com.shmrkm.chatworkWebhook.domain.model.auth.AccessToken

case class TokenAuthenticationMetadata(metadata: Metadata) {
  def token: Option[AccessToken] = metadata.getText("X-Authorization").map(AccessToken)
}
