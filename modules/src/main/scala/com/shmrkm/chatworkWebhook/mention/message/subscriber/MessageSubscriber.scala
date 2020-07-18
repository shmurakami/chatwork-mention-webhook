package com.shmrkm.chatworkWebhook.mention.message.subscriber

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.{Done, NotUsed}
import com.shmrkm.chatworkMention.repository.{AuthenticationRepositoryFactory, ChatworkApiClientFactory, ChatworkApiRepository, MentionRepositoryFactory, MentionStreamRepositoryFactory}
import com.shmrkm.chatworkWebhook.domain.model.chatwork.ApiToken
import com.shmrkm.chatworkWebhook.domain.model.message.Message
import com.shmrkm.chatworkWebhook.domain.model.query.message.QueryMessage
import com.shmrkm.chatworkWebhook.mention.message.subscriber.MessageSubscriberProxy.{ConsumeError, ConsumedMessage}
import com.typesafe.config.Config

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Try}

object MessageSubscriber {
  def props = Props(new MessageSubscriber)
  def name  = "message-subscriber"
}

class MessageSubscriber
    extends Actor
    with ActorLogging
    with MentionStreamRepositoryFactory
    with MentionRepositoryFactory
    with AuthenticationRepositoryFactory
    with ChatworkApiClientFactory {
  import io.circe.generic.auto._
  import io.circe.parser._

  implicit val ec: ExecutionContext = context.dispatcher
  implicit val config: Config       = context.system.settings.config

  implicit val mat: Materializer = Materializer(context)

  private val authRepository = factoryAuthenticationRepository()

  // subscribe requires connection for only subscribing
  private val mentionRepository = factoryMentionRepository()

  implicit val system: ActorSystem = context.system

  val chatworkApiRepository: ChatworkApiRepository = factoryChatworkApiClient()

  override def receive: Receive = {
    case message: ConsumedMessage => consumeFlow(message)

    case e: ConsumeError =>
      log.warning(s"error occurred $e")
      context.stop(self)
  }

  def consumeFlow(message: ConsumedMessage): Future[Try[Done]] = {
    // TODO set supervision strategy decider
    Source
      .single(message)
      .map { message => decode[Message](message.value).getOrElse(null) }
      .via(retrieveInsufficientDataAndBuildMessage)
      .via(updateReadModel)
      .toMat(Sink.head)(Keep.right)
      .run()
  }

  def retrieveInsufficientDataAndBuildMessage: Flow[Message, QueryMessage, NotUsed] = {
    Flow[Message]
      .mapAsync(1) { message =>
        authRepository.authenticationForAccountId(message.toAccountId)
          .flatMap {
            case Left(_) => throw new RuntimeException("")
            case Right(authentication) =>
              implicit val token: ApiToken = authentication.apiToken
              (for {
                room <- chatworkApiRepository.retrieveRoom(message.roomId)
                fromAccount <- chatworkApiRepository.retrieveAccount(message.roomId, message.fromAccountId)
              } yield Tuple2(room, fromAccount))
                .map { values =>
                  val room = values._1
                  val account = values._2

                  QueryMessage(
                    id = message.id,
                    roomId = message.roomId,
                    roomName = room.name,
                    roomIconUrl = room.iconUrl,
                    fromAccountId = message.fromAccountId,
                    fromAccountName = account.name,
                    fromAccountAvatarUrl = account.avatar,
                    toAccountId = message.toAccountId,
                    body = message.body,
                    sendTime = message.sendTime,
                    updateTime = message.updateTime
                  )
                }
          }
      }
  }

  def updateReadModel: Flow[QueryMessage, Try[Done], NotUsed] = {
    Flow[QueryMessage]
      .mapAsync(1) { message =>
        for {
          mentions <- mentionRepository.resolve(message.toAccountId)
          updatedMentions <- Future {
            mentions.add(message)
          }
          result <- mentionRepository.updateReadModel(message.toAccountId, updatedMentions)
        } yield result
          .recoverWith {
            case exception =>
              log.error(s"failure to update read model. should retry")
              Failure(exception)
            // then what's happened?
          }
      }
  }
}
