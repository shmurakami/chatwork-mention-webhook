package com.shmrkm.chatworkWebhook.mention.subscriber

import akka.actor.ActorSystem
import akka.stream.RestartSettings
import akka.testkit.{ ImplicitSender, TestKit, TestProbe }
import com.shmrkm.chatworkWebhook.concerns._
import com.shmrkm.chatworkWebhook.domain.model.account.AccountId
import com.shmrkm.chatworkWebhook.domain.model.auth.Authentication
import com.shmrkm.chatworkWebhook.domain.model.message._
import com.shmrkm.chatworkWebhook.domain.model.room.RoomId
import com.shmrkm.chatworkWebhook.mention.subscriber.MessageSubscriber.ConsumedMessage
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

class MessageSubscriberWorkerSpec
    extends TestKit(ActorSystem("message-susbcriber-worker"))
    with AnyWordSpecLike
    with Matchers
    with ImplicitSender
    with ScalaFutures
    with StopSystemAfterAll {

  import io.circe.generic.auto._
  import io.circe.syntax._

  implicit val ec: ExecutionContext = system.dispatcher

  "MessageSubscriberWorker" should {
    "retry if error occurred" in {

      val probe = TestProbe("retry-count-probe")

      def authenticationRepository = new TestAuthenticationRepository {
        override def resolve(accountId: AccountId): Future[Either[Throwable, Authentication]] = Future {
          probe.ref ! ""
          Left(new Exception())
        }
      }
      def streamRepository      = new TestStreamRepository      {}
      def mentionRepository     = new TestMentionRepository     {}
      def chatworkApiRepository = new TestChatworkApiRepository {}

      val restartSettings =
        RestartSettings(minBackoff = 100.millis, maxBackoff = 1.seconds, randomFactor = 0.1)
          .withMaxRestarts(3, 10.seconds)

      val messageSubscriberWorker = system.actorOf(
        MessageSubscriberWorker.props(
          authenticationRepository,
          streamRepository,
          mentionRepository,
          chatworkApiRepository,
          restartSettings
        )
      )

      val message = Message(
        id = MessageId("id"),
        roomId = RoomId(1L),
        fromAccountId = AccountId(1L),
        toAccountId = AccountId(2L),
        body = MessageBody("body"),
        sendTime = SendTime(0),
        updateTime = UpdateTime(0)
      )

      messageSubscriberWorker ! ConsumedMessage(message.asJson.noSpaces)

      probe.receiveN(3, 10.seconds)
    }
  }

}
