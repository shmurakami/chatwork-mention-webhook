package com.shmrkm.chatworkWebhook.mention.message.subscriber;

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import com.shmrkm.chatworkWebhook.concerns.StopSystemAfterAll
import com.shmrkm.chatworkWebhook.domain.model.account.AccountId
import com.shmrkm.chatworkWebhook.domain.model.message._
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
        |chatwork {
        |  api {
        |    url: "https://api.chatwork.com/v2"
        |    token = ""
        |  }
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
          id = MessageId("1330095363956146176"),
          roomId = RoomId(138410486L),
          fromAccountId = AccountId(567553L),
          toAccountId = AccountId(3073541L),
          body = MessageBody("hello"),
          sendTime = SendTime(1593593046),
          updateTime = UpdateTime(0)
        ).asJson.toString

        val subscriber = system.actorOf(MessageSubscriber.props, "subscriber")
        subscriber ! ConsumedMessage(messageJsonString)

        expectNoMessage(5 seconds)
      }
    }
  }

}
