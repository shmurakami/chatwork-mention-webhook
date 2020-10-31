package com.shmrkm.chatworkWebhook.mention.subscriber

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Keep, RestartSource, Sink, Source}
import akka.{Done, NotUsed}
import com.shmrkm.chatworkMention.repository._
import com.shmrkm.chatworkWebhook.domain.model.chatwork.ApiToken
import com.shmrkm.chatworkWebhook.domain.model.mention.MentionList
import com.shmrkm.chatworkWebhook.domain.model.message.Message
import com.shmrkm.chatworkWebhook.domain.model.query.message.QueryMessage
import com.shmrkm.chatworkWebhook.mention.subscriber.MessageSubscriber.{ConsumeError, ConsumedMessage}
import com.typesafe.config.Config

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Try}

object MessageSubscribeWorker {

  def props(
      authenticationRepository: AuthenticationRepository,
      mentionRepository: MentionRepository,
      chatworkApiRepository: ChatworkApiRepository
  ) =
    Props(new MessageSubscribeWorker(authenticationRepository, mentionRepository, chatworkApiRepository))
  def name = "message-subscribe-worker"
}

class MessageSubscribeWorker(
    authRepository: AuthenticationRepository,
    mentionRepository: MentionRepository,
    chatworkApiRepository: ChatworkApiRepository
) extends Actor
    with ActorLogging
    with MentionStreamRepositoryFactory {
  import io.circe.generic.auto._
  import io.circe.parser._

  implicit val ec: ExecutionContext = context.dispatcher
  implicit val config: Config       = context.system.settings.config

  implicit val mat: Materializer = Materializer(context)

  implicit val system: ActorSystem = context.system

  override def receive: Receive = {
    case message: ConsumedMessage => consumeFlow(message)

    case e: ConsumeError =>
      log.warning(s"error occurred $e")
      context.stop(self)
  }

  def consumeFlow(message: ConsumedMessage): Future[Try[Done]] = {
    val source =
      RestartSource.withBackoff(minBackoff = 3 seconds, maxBackoff = 10 seconds, randomFactor = 0.2, maxRestarts = 3)(
        () => Source.single(message)
      )

    source
      .map { message => decode[Message](message.value).getOrElse(null) }
      .via(retrieveInsufficientDataAndBuildMessage())
      .via(appendMessage())
      .via(updateReadModel())
      .toMat(Sink.head)(Keep.right)
      .run()
      .recover {
        case e: Throwable =>
          log.error(e.getMessage)
          Failure[Done](e)
      }
  }

  def retrieveInsufficientDataAndBuildMessage(): Flow[Message, QueryMessage, NotUsed] = {
    Flow[Message]
      .mapAsync(1) { message =>
        authRepository
          .authenticationForAccountId(message.toAccountId)
          .flatMap {
            case Left(_) => throw new RuntimeException("failed to resolve authentication")
            case Right(authentication) =>
              implicit val token: ApiToken = authentication.apiToken
              (for {
                room        <- chatworkApiRepository.retrieveRoom(message.roomId)
                fromAccount <- chatworkApiRepository.retrieveAccount(message.roomId, message.fromAccountId)
              } yield Tuple2(room, fromAccount))
                .map { values =>
                  val room    = values._1
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

  def appendMessage(): Flow[QueryMessage, MentionList, NotUsed] = {
    log.info("append")
    Flow[QueryMessage]
      .mapAsync(1) { message =>
        log.info("append body")
        val mentions = mentionRepository.resolve(message.toAccountId)
        mentions.map { mentionList => mentionList.add(message) }
      }
  }

  def updateReadModel(): Flow[MentionList, Try[Done], NotUsed] = {
    log.info("update")
    Flow[MentionList]
      .map { mentionList =>
        log.info(s"update list count: ${mentionList.list.length}")
        mentionRepository.updateReadModel(mentionList.list.head.toAccountId, mentionList)
      }
  }
}
