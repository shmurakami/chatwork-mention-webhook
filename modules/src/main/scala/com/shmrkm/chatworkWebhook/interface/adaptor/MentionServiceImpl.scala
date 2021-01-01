package com.shmrkm.chatworkWebhook.interface.adaptor

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.shmrkm.chatworkMention.repository.{ MentionStreamRepositoryFactory, StreamConsumer }
import com.shmrkm.chatworkWebhook.domain.model.account.AccountId
import com.shmrkm.chatworkWebhook.domain.model.query.message.QueryMessage
import com.shmrkm.chatworkWebhook.mention.protocol.query.MentionQuery
import com.shmrkm.chatworkWebhook.mention.usecase.MentionListUseCase
import org.reactivestreams.{ Publisher, Subscriber }

import scala.concurrent.{ ExecutionContext, Future }

// TODO create new proto for subscribe method
class MentionServiceImpl[T](mentionListUseCase: MentionListUseCase, _publisher: Publisher[String] = null)(implicit val ec: ExecutionContext)
    extends MentionService
    with MentionStreamRepositoryFactory {

  override def list(in: MentionListRequest): Future[MentionListReply] = {
    mentionListUseCase.execute(MentionQuery(AccountId(in.accountId))).map {
      case Right(mentionListResponse) =>
        MentionListReply(mentionListResponse.mentionList.list.map(queryMessage2MentionReply))
      case Left(error) => throw new Exception(error)
    }
  }

  override def subscribe(in: MentionSubscribeRequest): Source[MentionReply, NotUsed] = {
    val publisher: Publisher[String] = _publisher match {
      case null => new Publisher[String] {
        override def subscribe(s: Subscriber[_ >: String]): Unit = {
          val mentionStreamRepository = factoryStreamRepository()
          mentionStreamRepository.subscribePushNotification(new StreamConsumer {
            override def onSubscribe(channel: String): Unit   = ()
            override def onUnsubscribe(channel: String): Unit = ()
            override def onMessage(message: String): Unit     = s.onNext(message)
            override def onError(e: Throwable): Unit          = s.onError(e)
          })
        }
      }
      case p => p
    }

    Source.fromPublisher(publisher).map { message =>
      import io.circe.parser._
      import io.circe.generic.auto._
      decode[QueryMessage](message) match {
        // TODO check account ID. actually should subscribing filter with given account id
        case Right(queryMessage) => queryMessage2MentionReply(queryMessage)
        case Left(e)             => throw e
      }
    }
  }

  private def queryMessage2MentionReply(queryMessage: QueryMessage): MentionReply = {
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
  }
}
