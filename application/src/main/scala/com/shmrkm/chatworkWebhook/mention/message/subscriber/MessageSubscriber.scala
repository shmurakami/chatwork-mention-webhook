package com.shmrkm.chatworkWebhook.mention.message.subscriber

import akka.actor.{Actor, ActorLogging, Props}
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.{Done, NotUsed}
import com.redis._
import com.shmrkm.chatworkWebhook.domain.model.account.{AccountName, FromAccountAvatarUrl, ToAccountId}
import com.shmrkm.chatworkWebhook.domain.model.mention.MentionMessage
import com.shmrkm.chatworkWebhook.domain.model.message.Message
import com.shmrkm.chatworkWebhook.domain.model.query.message.QueryMessage
import com.shmrkm.chatworkWebhook.domain.model.room.{RoomIconUrl, RoomName}
import com.shmrkm.chatworkWebhook.mention.MentionStreamRepositoryFactory
import com.shmrkm.chatworkWebhook.mention.message.subscriber.MessageSubscriber.{ConsumeError, ConsumedMessage}
import com.shmrkm.chatworkWebhook.mention.message.subscriber.MessageSubscriberProxy.Start
import com.typesafe.config.Config

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object MessageSubscriber {
  case class ConsumedMessage(value: String)
  case class ConsumeError(ex: Throwable)

  def props = Props(new MessageSubscriber)
  def name  = "message-subscriber"
}

class MessageSubscriber extends Actor with ActorLogging with MentionStreamRepositoryFactory {
  import io.circe.generic.auto._
  import io.circe.parser._

  implicit val ec: ExecutionContext = context.system.dispatcher
  implicit val config: Config       = context.system.settings.config

  implicit val mat: Materializer = Materializer(context)

  val mentionRepository = factoryMentionRepository()
  val channelName       = config.getString("redis.channel-name")

  override def receive: Receive = {
    case _: Start => mentionRepository.subscribe(channelName)(registerConsumerReceiver)

    case message: ConsumedMessage => consumeFlow(message)

    case e: ConsumeError => log.warning(s"error occurred $e")
  }

  def registerConsumerReceiver: PubSubMessage => Unit = {
    case S(channel: String, _) => log.info(s"redis channel $channel subscribed")
    case U(channel: String, _) => log.info(s"unsubscribed redis channel $channel")
    case M(origChannel: String, message: String) =>
      log.info(s"message $message published to channel $origChannel")
      self ! ConsumedMessage(message)
    case E(e) => log.warning(s"subscribing error occurred $e")
  }

  def consumeFlow(message: ConsumedMessage): Future[Done] = {

    /**
      * whats to do?
      * get message
      * json parse && map to domain.Message
      * collect insufficient information from CW api
      * update read model
      * done
      */
    Source
      .single(message)
      .map { message => decode[Message](message.value).getOrElse(null) }
      .via(retrieveInsufficientDataAndBuildMessage)
      .via(updateReadModel)
      .toMat(Sink.head)(Keep.right)
      .run()
  }

  def retrieveInsufficientDataAndBuildMessage(): Flow[Message, QueryMessage, NotUsed] = {
    // TODO retrieve some data from cw api
    Flow[Message]
      .map { message =>
        log.info(s"$message")
        QueryMessage(
          id = message.id,
          roomId = message.roomId,
          roomName = RoomName(""),
          roomIconUrl = RoomIconUrl(""),
          fromAccountId = message.fromAccountId,
          fromAccountName = AccountName(""),
          fromAccountAvatarUrl = FromAccountAvatarUrl(""),
          toAccountId = message.toAccountId,
          body = message.body,
          sendTime = message.sendTime,
          updateTime = message.updateTime
        )
      }
  }

  def updateReadModel(): Flow[QueryMessage, Done, NotUsed] = {
    Flow[QueryMessage]
      .map { message =>
        for {
          mentions        <- mentionRepository.resolve(message.toAccountId)
          updatedMentions <- Future { mentions.add(message) }
          result          <- mentionRepository.updateReadModel(message.toAccountId, updatedMentions)
        } yield result
          .recoverWith {
            case exception =>
              log.error(s"failure to update read model. should retry")
              Failure(exception)
              // then what's happened?
          }
        Done
      }
  }
}
