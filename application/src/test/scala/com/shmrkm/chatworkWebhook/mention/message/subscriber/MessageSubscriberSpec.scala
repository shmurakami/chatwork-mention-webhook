package com.shmrkm.chatworkWebhook.mention.message.subscriber;

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import com.shmrkm.chatworkWebhook.concerns.StopSystemAfterAll
import com.shmrkm.chatworkWebhook.domain.model.account.{FromAccountId, ToAccountId}
import com.shmrkm.chatworkWebhook.domain.model.message.{Message, MessageBody, MessageId, SendTime, UpdateTime}
import com.shmrkm.chatworkWebhook.domain.model.room.RoomId
import com.shmrkm.chatworkWebhook.mention.message.subscriber.MessageSubscriber.ConsumedMessage
import com.typesafe.config.ConfigFactory
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration._

class MessageSubscriberSpec
    extends TestKit(
      ActorSystem(
        "message-subscriber",
        ConfigFactory.parseString("""
        |redis {
        |  host = "127.0.0.1"
        |  port = 6379
        |  channel-name = "spec"
        |}
        |""".stripMargin)
      )
    )
    with AnyWordSpecLike
    with Matchers
    with ImplicitSender
    with StopSystemAfterAll {

  "subscriber" when {
    "got ConsumedMessage" should {
      "run update readmodel flow" in {
        import io.circe.generic.auto._
        import io.circe.syntax._

        val messageJsonString = Message(
          id = MessageId("123"),
          roomId = RoomId(234L),
          fromAccountId = FromAccountId(345L),
          toAccountId = ToAccountId(456L),
          body = MessageBody("test message"),
          sendTime = SendTime(0),
          updateTime = UpdateTime(0)
        ).asJson.toString

        val subscriber = system.actorOf(MessageSubscriber.props, "subscriber")
        subscriber ! ConsumedMessage(messageJsonString)

        expectNoMessage(3 seconds)
      }
    }
  }

}
