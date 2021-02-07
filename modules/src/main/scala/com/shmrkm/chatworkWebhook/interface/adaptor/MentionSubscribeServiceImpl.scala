package com.shmrkm.chatworkWebhook.interface.adaptor

import java.util.concurrent.TimeoutException

import akka.NotUsed
import akka.grpc.scaladsl.Metadata
import akka.stream.scaladsl.Source
import com.shmrkm.chatworkMention.repository.{AuthenticationRepository, MentionStreamRepositoryFactory, StreamConsumer}
import com.shmrkm.chatworkWebhook.auth.exception.AuthenticationFailureException
import com.shmrkm.chatworkWebhook.domain.model.account.AccountId
import com.shmrkm.chatworkWebhook.domain.model.auth.Authentication
import com.shmrkm.chatworkWebhook.domain.model.query.message.QueryMessage
import org.reactivestreams.{Publisher, Subscriber}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}

class MentionSubscribeServiceImpl(publisher: Publisher[String] = null, override implicit val authenticationRepository: AuthenticationRepository)(implicit val ec: ExecutionContext)
    extends MentionSubscribeServicePowerApi
    with MentionStreamRepositoryFactory
    with MentionServiceReplier
    with TokenAuthorizer {

  override def subscribe(in: MentionSubscribeRequest, metadata: Metadata): Source[MentionReply, NotUsed] = {
    val authFuture = authorize(AccountId(in.accountId), TokenAuthenticationMetadata(metadata).token)

    try {
      // FIXME blocking is bad approach. how to do Future authentication and then return Source?
      Await.result(authFuture, 3 seconds) match {
        case _: Authentication => execute(in)
        case _ => Source.failed(AuthenticationFailureException())
      }
    } catch {
      case _: TimeoutException => Source.failed(AuthenticationFailureException())
      case ex: AuthenticationFailureException => Source.failed(ex)
    }
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
