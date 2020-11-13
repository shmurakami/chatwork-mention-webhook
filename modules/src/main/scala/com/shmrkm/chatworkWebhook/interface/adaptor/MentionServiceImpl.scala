package com.shmrkm.chatworkWebhook.interface.adaptor
import akka.NotUsed
import akka.stream.scaladsl.Source

import scala.concurrent.Future

class MentionServiceImpl extends MentionService {
  override def list(in: MentionListRequest): Future[MentionListReply] = ???

  override def subscribe(in: MentionSubscribeRequest): Source[MentionReply, NotUsed] = ???
}
