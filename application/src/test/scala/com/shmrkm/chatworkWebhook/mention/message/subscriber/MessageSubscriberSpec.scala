package com.shmrkm.chatworkWebhook.mention.message.subscriber;

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import com.shmrkm.chatworkWebhook.concerns.StopSystemAfterAll
import com.shmrkm.chatworkWebhook.mention.message.subscriber.MessageSubscriberProxy.Start
import com.typesafe.config.ConfigFactory
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike;

import scala.concurrent.duration._

class MessageSubscriberSpec
    extends TestKit(ActorSystem("message-subscriber", ConfigFactory.parseString(
      """
        |redis {
        |  host = "127.0.0.1"
        |  port = 6379
        |  channel-name = "spec"
        |}
        |""".stripMargin)))
    with AnyWordSpecLike
    with Matchers
    with ImplicitSender
    with StopSystemAfterAll {

  "subscriber" when {
    "got Start" should {
      "reply Sample Message to ref" in {
        val testProbe = TestProbe()

        val subscriber = system.actorOf(MessageSubscriber.props, "subscriber")
        subscriber ! Start()
        within(10 seconds) {
          testProbe expectMsg "ok"
        }
      }
    }
  }

}
