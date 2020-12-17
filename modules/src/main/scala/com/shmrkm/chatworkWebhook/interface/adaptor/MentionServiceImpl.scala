package com.shmrkm.chatworkWebhook.interface.adaptor

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.shmrkm.chatworkWebhook.domain.model.account.AccountId
import com.shmrkm.chatworkWebhook.domain.model.query.message.QueryMessage
import com.shmrkm.chatworkWebhook.mention.protocol.query.MentionQuery
import com.shmrkm.chatworkWebhook.mention.usecase.MentionListUseCase

import scala.concurrent.{ExecutionContext, Future}

class MentionServiceImpl(mentionListUseCase: MentionListUseCase)(implicit ec: ExecutionContext) extends MentionService {

  override def list(in: MentionListRequest): Future[MentionListReply] = {
    mentionListUseCase.execute(MentionQuery(AccountId(in.accountId))).map {
      case Right(mentionListResponse) =>
        MentionListReply(mentionListResponse.mentionList.list.map { queryMessage: QueryMessage =>
          MentionReply(
            id = queryMessage.id.value,
            roomId = queryMessage.roomId.value,
            roomName = queryMessage.roomName.value,
            roomIconUrl = queryMessage.roomIconUrl.value,
            fromAccountId = queryMessage.fromAccountId.value.toInt,
            fromAccountName = queryMessage.fromAccountName.value,
            fromAccountAvatarUrl = queryMessage.fromAccountAvatarUrl.value,
            toAccountId = queryMessage.toAccountId.value.toInt,
            body = queryMessage.body.value,
            sendTime = queryMessage.sendTime.value,
            updateTime = queryMessage.updateTime.value
          )
        })
      case Left(error) => throw new Exception(error)
    }
  }

  override def subscribe(in: MentionSubscribeRequest): Source[MentionReply, NotUsed] = ???
}
