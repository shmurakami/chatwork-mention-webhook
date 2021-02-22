package com.shmrkm.chatworkWebhook.mention.usecase


import com.shmrkm.chatworkWebhook.mention.protocol.query.{MentionListResponse, MentionQuery}

import scala.concurrent.Future

trait MentionListUseCase {

  def execute(query: MentionQuery): Future[Either[String, MentionListResponse]]

}
