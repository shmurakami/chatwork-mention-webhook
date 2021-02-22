package com.shmrkm.chatworkWebhook.mention.usecase

import com.shmrkm.chatworkMention.exception.{ InvalidAccountIdException, RequestFailureException }
import com.shmrkm.chatworkMention.repository.MentionRepository
import com.shmrkm.chatworkWebhook.mention.protocol.query.{ MentionListResponse, MentionQuery }
import com.typesafe.scalalogging.Logger

import scala.concurrent.{ ExecutionContext, Future }

class MentionListUseCaseImpl(mentionRepository: MentionRepository)(implicit ec: ExecutionContext)
    extends MentionListUseCase {
  private val logger = Logger(classOf[MentionListUseCase])

  override def execute(query: MentionQuery): Future[Either[String, MentionListResponse]] = {
    mentionRepository
      .fetch(query.accountId)
      .map(mentionList => Right(MentionListResponse(mentionList)))
      .recover {
        case e: InvalidAccountIdException =>
          logger.warn(e.toString)
          Left("error")
        case e: RequestFailureException =>
          logger.warn(e.toString)
          Left("try again")
      }
  }
}
