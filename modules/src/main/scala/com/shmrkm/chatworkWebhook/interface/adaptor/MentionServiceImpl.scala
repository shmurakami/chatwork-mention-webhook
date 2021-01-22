package com.shmrkm.chatworkWebhook.interface.adaptor

import akka.grpc.scaladsl.Metadata
import com.shmrkm.chatworkWebhook.domain.model.account.AccountId
import com.shmrkm.chatworkWebhook.mention.protocol.query.MentionQuery
import com.shmrkm.chatworkWebhook.mention.usecase.MentionListUseCase

import scala.concurrent.{ExecutionContext, Future}

class MentionServiceImpl(mentionListUseCase: MentionListUseCase)(implicit val ec: ExecutionContext)
    extends MentionServicePowerApi
    with MentionServiceReplier {

  override def list(in: MentionListRequest, metadata: Metadata): Future[MentionListReply] = {
    mentionListUseCase.execute(MentionQuery(AccountId(in.accountId))).map {
      case Right(mentionListResponse) =>
        MentionListReply(mentionListResponse.mentionList.list.map(queryMessage2MentionReply))
      case Left(error) => throw new Exception(error)
    }
  }
}
