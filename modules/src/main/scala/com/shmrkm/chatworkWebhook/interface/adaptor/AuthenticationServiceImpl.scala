package com.shmrkm.chatworkWebhook.interface.adaptor
import scala.concurrent.Future

class AuthenticationServiceImpl extends AuthenticationService {
  override def auth(in: AuthenticationRequest): Future[AuthenticationReply] = ???
}
