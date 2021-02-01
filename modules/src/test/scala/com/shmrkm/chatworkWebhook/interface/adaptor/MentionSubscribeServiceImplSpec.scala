package com.shmrkm.chatworkWebhook.interface.adaptor

import akka.actor.ActorSystem
import akka.grpc.scaladsl.{Metadata, MetadataEntry}
import akka.stream.scaladsl.Sink
import akka.testkit.TestKit
import akka.util.ByteString
import com.shmrkm.chatworkMention.repository.AuthenticationRepository
import com.shmrkm.chatworkWebhook.domain.model.account.{AccountId, AccountName, FromAccountAvatarUrl}
import com.shmrkm.chatworkWebhook.domain.model.auth.{AccessToken, Authentication}
import com.shmrkm.chatworkWebhook.domain.model.chatwork.ApiToken
import com.shmrkm.chatworkWebhook.domain.model.message.{MessageBody, MessageId, SendTime, UpdateTime}
import com.shmrkm.chatworkWebhook.domain.model.query.message.QueryMessage
import com.shmrkm.chatworkWebhook.domain.model.room.{RoomIconUrl, RoomId, RoomName}
import org.reactivestreams.{Publisher, Subscriber}
import org.scalatest.concurrent.PatienceConfiguration.{Interval, Timeout}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Seconds, Span}
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class MentionSubscribeServiceImplSpec
    extends TestKit(ActorSystem())
    with AnyWordSpecLike
    with Matchers
    with ScalaFutures {

  val queryMessage = QueryMessage(
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
  )

  "MentionSubscribeService" should {
    "return stream of new mention" in {
      import io.circe.generic.auto._
      import io.circe.syntax._

      implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

      val publisher: Publisher[String] = new Publisher[String] {
        override def subscribe(s: Subscriber[_ >: String]): Unit = {
          // give json string will be parsed to object and then reply
          val queryMessageAsString = queryMessage.asJson.noSpaces
          s.onNext(queryMessageAsString)

          s.onComplete()
        }
      }

      val authenticationRepository = new AuthenticationRepository {
        def resolve(accountId: AccountId): Future[Either[Throwable, Authentication]] = Future {
          Right(Authentication(AccountId(1), ApiToken(""), AccessToken("token")))
        }
        def issueAccessToken(authentication: Authentication): Future[Try[AccessToken]] = ???
      }

      val subscribeService = new MentionSubscribeServiceImpl(publisher, authenticationRepository)

      val token = "token"
      val metadata = new Metadata {
        override def getText(key: String): Option[String] = key match {
          case "X-Authentication" => Some(token)
          case _ => None
        }
        override def getBinary(key: String): Option[ByteString] = ???
        override def asMap: Map[String, List[MetadataEntry]] = ???
        override def asList: List[(String, MetadataEntry)] = ???
      }

      val source       = subscribeService.subscribe(MentionSubscribeRequest(accountId = 1), metadata)
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
