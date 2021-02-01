package com.shmrkm.chatworkWebhook.interface.adaptor

import akka.grpc.scaladsl.Metadata
import com.shmrkm.chatworkMention.repository.AuthenticationRepository
import com.shmrkm.chatworkWebhook.domain.model.account.AccountId
import com.shmrkm.chatworkWebhook.mention.protocol.query.MentionQuery
import com.shmrkm.chatworkWebhook.mention.usecase.MentionListUseCase

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class MentionServiceImpl(mentionListUseCase: MentionListUseCase, override implicit val authenticationRepository: AuthenticationRepository)(implicit val ec: ExecutionContext)
    extends MentionServicePowerApi
    with MentionServiceReplier
    with TokenAuthorizer {

  override def list(in: MentionListRequest, metadata: Metadata): Future[MentionListReply] = {
    Try(authorize(AccountId(in.accountId), TokenAuthenticationMetadata(metadata).token)) match {
      case Success(_) => execute(in)
      case Failure(ex) => Future.failed(ex)
    }
  }

  private def execute(in: MentionListRequest): Future[MentionListReply] = {
    mentionListUseCase.execute(MentionQuery(AccountId(in.accountId))).map {
      case Right(mentionListResponse) =>
        MentionListReply(mentionListResponse.mentionList.list.map(queryMessage2MentionReply))
      case Left(error) => throw new Exception(error)
    }
  }
}
