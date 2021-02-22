package com.shmrkm.chatworkWebhook.interface.adaptor

import akka.NotUsed
import akka.grpc.scaladsl.Metadata
import akka.stream.scaladsl.Source
import com.shmrkm.chatworkMention.repository.{AuthenticationRepository, MentionStreamRepositoryFactory, StreamConsumer}
import com.shmrkm.chatworkWebhook.domain.model.account.AccountId
import com.shmrkm.chatworkWebhook.domain.model.query.message.QueryMessage
import org.reactivestreams.{Publisher, Subscriber}

import scala.concurrent.ExecutionContext

class MentionSubscribeServiceImpl(publisher: Publisher[String] = null, override implicit val authenticationRepository: AuthenticationRepository)(implicit val ec: ExecutionContext)
    extends MentionSubscribeServicePowerApi
    with MentionStreamRepositoryFactory
    with MentionServiceReplier
    with TokenAuthorizer {

  case class SubscribeRequest(payload: MentionSubscribeRequest, metadata: Metadata)

  override def subscribe(in: MentionSubscribeRequest, metadata: Metadata): Source[MentionReply, NotUsed] = {
    Source.single(SubscribeRequest(in, metadata))
      .mapAsyncUnordered(1) { request => {
        authorize(AccountId(in.accountId), TokenAuthenticationMetadata(metadata).token).map {
          case Left(ex) => {
            println("throw", ex)
            throw ex
          }
          case Right(_) => request
        }
      }}
      .flatMapMerge(breadth = 1, { request =>
        execute(request.payload)
      })
//      .recoverWithRetries(attempts = 1, {
//        case ex => Source.failed(ex)
//      })
  }

  private def execute(in: MentionSubscribeRequest): Source[MentionReply, NotUsed] = {
    // TODO filter by in.accountId
    Source.fromPublisher(resolvePublisher)
      .map { message =>
      import io.circe.generic.auto._
      import io.circe.parser._
      decode[QueryMessage](message) match {
        // TODO check account ID. actually should subscribing filter with given account id
        case Right(queryMessage) => queryMessage2MentionReply(queryMessage)
        case Left(e)             => throw e
      }
    }
  }

  private def resolvePublisher: Publisher[String] = {
    def defaultPublisher: Publisher[String] = {
      new Publisher[String] {
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
    }

    publisher match {
      case null => defaultPublisher
      case p    => p
    }
  }
}
