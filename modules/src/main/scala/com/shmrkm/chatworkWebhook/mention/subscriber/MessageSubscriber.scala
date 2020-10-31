package com.shmrkm.chatworkWebhook.mention.subscriber

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import com.redis._
import com.shmrkm.chatworkMention.repository.{AuthenticationRepositoryFactory, ChatworkApiClientFactory, MentionRepositoryFactory, MentionStreamRepositoryFactory}
import com.shmrkm.chatworkWebhook.actor.ChildLookup
import com.shmrkm.chatworkWebhook.mention.subscriber.MessageSubscriber.{ConsumeError, ConsumedMessage}
import com.shmrkm.chatworkWebhook.mention.subscriber.MessageSubscriberProxy.Start
import com.typesafe.config.Config

import scala.concurrent.ExecutionContext

object MessageSubscriber {
  sealed trait Command
  case class ConsumedMessage(value: String) extends Command
  case class ConsumeError(ex: Throwable)    extends Command

  def props: Props = Props(new MessageSubscriber())
  def name         = "message-subscriber"
}

class MessageSubscriber
    extends Actor
    with ActorLogging
    with MentionStreamRepositoryFactory
    with ChildLookup
    with AuthenticationRepositoryFactory
    with MentionRepositoryFactory
    with ChatworkApiClientFactory {
  override type Command = MessageSubscriber.Command

  override implicit val system: ActorSystem = context.system

  implicit val ec: ExecutionContext = context.dispatcher
  implicit val config: Config       = context.system.settings.config

  private val subscriberRepository = factoryStreamRepository()

  override def receive: Receive = {
    case _: Start =>
      subscriberRepository.subscribe(subscriber)

    case cmd: Command =>
      context
        .child(MessageSubscribeWorker.name).fold(
          createAndForward(
            MessageSubscribeWorker.props(factoryAuthenticationRepository(), factoryMentionRepository(), factoryChatworkApiClient()),
            MessageSubscribeWorker.name
          )(cmd)
        )(forwardCmd(cmd))
  }

  def subscriber: PubSubMessage => Unit = {
    case S(channel: String, _) => log.info(s"redis channel $channel subscribed")
    case U(channel: String, _) => log.info(s"unsubscribed redis channel $channel")
    case M(origChannel: String, message: String) =>
      log.info(s"message published to channel $origChannel")
      self ! ConsumedMessage(message)
    case E(ex) =>
      log.error(s"subscribing error occurred $ex")
      self ! ConsumeError(ex)
  }
}
