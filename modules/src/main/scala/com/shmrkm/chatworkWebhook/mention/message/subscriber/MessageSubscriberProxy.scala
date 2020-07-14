package com.shmrkm.chatworkWebhook.mention.message.subscriber

import akka.actor.{ Actor, ActorLogging, ActorRef, Props }
import com.redis._
import com.shmrkm.chatworkMention.repository.MentionStreamRepositoryFactory
import com.shmrkm.chatworkWebhook.mention.message.subscriber.MessageSubscriberProxy.{
  Command,
  ConsumeError,
  ConsumedMessage,
  Start
}
import com.typesafe.config.Config

import scala.concurrent.ExecutionContext

object MessageSubscriberProxy {
  sealed trait Command
  case class Start()                        extends Command
  case class ConsumedMessage(value: String) extends Command
  case class ConsumeError(ex: Throwable)    extends Command

  def props = Props(new MessageSubscriberProxy)

  def name = "message-subscriber-proxy"
}

class MessageSubscriberProxy extends Actor with ActorLogging with MentionStreamRepositoryFactory {
  implicit val ec: ExecutionContext = context.dispatcher
  implicit val config: Config       = context.system.settings.config

  private val subscriberRepository = factoryStreamRepository()

  override def receive: Receive = {
    case _: Start => subscriberRepository.subscribe(subscriber)

    case cmd: Command => context.child(MessageSubscriber.name).fold(createAndForward(cmd))(forwardCmd(cmd))
  }

  private def createAndForward(command: Command) = createSubscriber() forward command

  private def createSubscriber(): ActorRef = context.actorOf(MessageSubscriber.props, MessageSubscriber.name)

  private def forwardCmd(command: Command)(ref: ActorRef) = ref forward command

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
