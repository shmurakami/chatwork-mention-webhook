package com.shmrkm.chatworkWebhook.mention.subscriber

import akka.actor.{ Actor, ActorLogging, ActorSystem, Props }
import com.shmrkm.chatworkMention.repository._
import com.shmrkm.chatworkWebhook.actor.ChildLookup
import com.shmrkm.chatworkWebhook.mention.subscriber.MessageSubscriber.{ ConsumeError, ConsumedMessage }
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
      subscriberRepository.subscribeWebhookFlow(subscriber)

    case cmd: Command =>
      context
        .child(MessageSubscriberWorker.name).fold(
          createAndForward(
            MessageSubscriberWorker.props(
              authenticationRepository = factoryAuthenticationRepository(),
              streamRepository = factoryStreamRepository(),
              mentionRepository = factoryMentionRepository(),
              chatworkApiRepository = factoryChatworkApiClient()
            ),
            MessageSubscriberWorker.name
          )(cmd)
        )(forwardCmd(cmd))
  }

  def subscriber: StreamConsumer = new StreamConsumer {
    override def onSubscribe(channel: String): Unit = log.info(s"redis channel $channel subscribed")

    override def onUnsubscribe(channel: String): Unit = log.info(s"unsubscribed redis channel $channel")

    override def onMessage(message: String): Unit = self ! ConsumedMessage(message)

    override def onError(ex: Throwable): Unit = {
      log.error(s"subscribing error occurred $ex")
      self ! ConsumeError(ex)
    }
  }
}
