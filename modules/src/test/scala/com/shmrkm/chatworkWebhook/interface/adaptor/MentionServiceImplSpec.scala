package com.shmrkm.chatworkWebhook.interface.adaptor

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{ HttpEntity, HttpMessage, HttpMethod, HttpMethods, HttpRequest }
import akka.stream.scaladsl.Sink
import akka.testkit.TestKit
import com.shmrkm.chatworkWebhook.domain.model.account.{ AccountId, AccountName, FromAccountAvatarUrl }
import com.shmrkm.chatworkWebhook.domain.model.mention.MentionList
import com.shmrkm.chatworkWebhook.domain.model.message.{ MessageBody, MessageId, SendTime, UpdateTime }
import com.shmrkm.chatworkWebhook.domain.model.query.message.QueryMessage
import com.shmrkm.chatworkWebhook.domain.model.room.{ RoomIconUrl, RoomId, RoomName }
import com.shmrkm.chatworkWebhook.mention.protocol.query.{ MentionListResponse, MentionQuery }
import com.shmrkm.chatworkWebhook.mention.usecase.MentionListUseCase
import org.reactivestreams.{ Publisher, Subscriber, Subscription }
import org.scalatest.concurrent.PatienceConfiguration.{ Interval, Timeout }
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{ Seconds, Span }
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.{ ExecutionContext, Future }

class MentionServiceImplSpec extends TestKit(ActorSystem()) with AnyWordSpecLike with Matchers with ScalaFutures {

  val queryMessages: Vector[QueryMessage] = Vector(
    QueryMessage(
      id = MessageId("1"),
      roomId = RoomId(2L),
      roomName = RoomName("room2"),
      roomIconUrl = RoomIconUrl("roomIcon2"),
      fromAccountId = AccountId(3),
      fromAccountName = AccountName("from3"),
      fromAccountAvatarUrl = FromAccountAvatarUrl("fromAvatar3"),
      toAccountId = AccountId(1),
      body = MessageBody("body1"),
      sendTime = SendTime(0),
      updateTime = UpdateTime(0)
    ),
    QueryMessage(
      id = MessageId("10"),
      roomId = RoomId(20L),
      roomName = RoomName("room20"),
      roomIconUrl = RoomIconUrl("roomIcon20"),
      fromAccountId = AccountId(30),
      fromAccountName = AccountName("from30"),
      fromAccountAvatarUrl = FromAccountAvatarUrl("fromAvatar30"),
      toAccountId = AccountId(1),
      body = MessageBody("body10"),
      sendTime = SendTime(0),
      updateTime = UpdateTime(0)
    )
  )

  "MentionService" should {
    "return list of mention" in {
      implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

      val mentionListUseCase: MentionListUseCase = (mentionQuery: MentionQuery) =>
        Future {
          Right(
            MentionListResponse(
              MentionList(queryMessages)
            )
          )
        }

      val mentionService = new MentionServiceImpl(mentionListUseCase)
      val mentionReply   = mentionService.list(MentionListRequest(accountId = 1)).futureValue

      val expect = MentionListReply(
        Vector(
          MentionReply(
            id = "1",
            roomId = 2L,
            roomName = "room2",
            roomIconUrl = "roomIcon2",
            fromAccountId = 3,
            fromAccountName = "from3",
            fromAccountAvatarUrl = "fromAvatar3",
            toAccountId = 1,
            body = "body1"
          ),
          MentionReply(
            id = "10",
            roomId = 20L,
            roomName = "room20",
            roomIconUrl = "roomIcon20",
            fromAccountId = 30,
            fromAccountName = "from30",
            fromAccountAvatarUrl = "fromAvatar30",
            toAccountId = 1,
            body = "body10"
          )
        )
      )

      mentionReply shouldBe expect
    }

    "return stream of new mention" in {
      import io.circe.syntax._
      import io.circe.generic.auto._

      implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

      val mentionListUseCase: MentionListUseCase = (query: MentionQuery) =>
        Future {
          Right(MentionListResponse(MentionList(Seq.empty)))
        }

      val publisher = new Publisher[String] {
        override def subscribe(s: Subscriber[_ >: String]): Unit = {
          // give json string will be parsed to object and then reply
          val queryMessageAsString = queryMessages.head.asJson.noSpaces
          s.onNext(queryMessageAsString)

          s.onComplete()
        }
      }

      val mentionService = new MentionServiceImpl(mentionListUseCase, publisher)

      val source       = mentionService.subscribe(MentionSubscribeRequest(accountId = 1))
      val mentionReply = source.runWith(Sink.head).futureValue(Timeout(Span(5L, Seconds)), Interval(Span(1L, Seconds)))
      val expect = MentionReply(
        id = "1",
        roomId = 2L,
        roomName = "room2",
        roomIconUrl = "roomIcon2",
        fromAccountId = 3,
        fromAccountName = "from3",
        fromAccountAvatarUrl = "fromAvatar3",
        toAccountId = 1,
        body = "body1",
        sendTime = 0,
        updateTime = 0
      )
      mentionReply shouldEqual expect
    }
  }

}
