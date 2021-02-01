package com.shmrkm.chatworkWebhook.interface.adaptor

import akka.actor.ActorSystem
import akka.grpc.scaladsl.{Metadata, MetadataEntry}
import akka.testkit.TestKit
import akka.util.ByteString
import com.shmrkm.chatworkMention.repository.AuthenticationRepository
import com.shmrkm.chatworkWebhook.domain.model.account.{AccountId, AccountName, FromAccountAvatarUrl}
import com.shmrkm.chatworkWebhook.domain.model.auth.{AccessToken, Authentication}
import com.shmrkm.chatworkWebhook.domain.model.chatwork.ApiToken
import com.shmrkm.chatworkWebhook.domain.model.mention.MentionList
import com.shmrkm.chatworkWebhook.domain.model.message.{MessageBody, MessageId, SendTime, UpdateTime}
import com.shmrkm.chatworkWebhook.domain.model.query.message.QueryMessage
import com.shmrkm.chatworkWebhook.domain.model.room.{RoomIconUrl, RoomId, RoomName}
import com.shmrkm.chatworkWebhook.mention.protocol.query.{MentionListResponse, MentionQuery}
import com.shmrkm.chatworkWebhook.mention.usecase.MentionListUseCase
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

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

      val authenticationRepository = new AuthenticationRepository {
        def resolve(accountId: AccountId): Future[Either[Throwable, Authentication]] = Future {
          Right(Authentication(AccountId(1), ApiToken(""), AccessToken("token")))
        }
        def issueAccessToken(authentication: Authentication): Future[Try[AccessToken]] = ???
      }

      val metadata = new Metadata {
        override def getText(key: String): Option[String] = key match {
          case "X-Authorization" => Some("token")
          case _ => None
        }
        override def getBinary(key: String): Option[ByteString] = ???
        override def asMap: Map[String, List[MetadataEntry]] = ???
        override def asList: List[(String, MetadataEntry)] = ???
      }

      val mentionService = new MentionServiceImpl(mentionListUseCase, authenticationRepository)
      val mentionReply   = mentionService.list(MentionListRequest(accountId = 1), metadata).futureValue

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
  }

}
