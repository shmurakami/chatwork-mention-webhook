package com.shmrkm.chatworkWebhook.mention.subscriber

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import com.redis.RedisClient
import com.shmrkm.chatworkMention.repository.{AuthenticationRepository, ChatworkApiRepository, MeResponse, MentionRepositoryRedisImpl, StreamConsumer, StreamRepository}
import com.shmrkm.chatworkWebhook.concerns.StopSystemAfterAll
import com.shmrkm.chatworkWebhook.domain.model.account.{AccountId, AccountName, FromAccount, FromAccountAvatarUrl}
import com.shmrkm.chatworkWebhook.domain.model.auth.{AccessToken, Authentication}
import com.shmrkm.chatworkWebhook.domain.model.chatwork.{ApiToken, Me}
import com.shmrkm.chatworkWebhook.domain.model.mention.MentionList
import com.shmrkm.chatworkWebhook.domain.model.message._
import com.shmrkm.chatworkWebhook.domain.model.query.message.QueryMessage
import com.shmrkm.chatworkWebhook.domain.model.room.{Room, RoomIconUrl, RoomId, RoomName}
import com.shmrkm.chatworkWebhook.mention.subscriber.MessageSubscriber.ConsumedMessage
import com.typesafe.config.ConfigFactory
import org.scalatest.concurrent.PatienceConfiguration.{Interval, Timeout}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.{Success, Try}

object MessageSubscriberSpec {
  val config = ConfigFactory.parseString("""
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
}

import MessageSubscriberSpec._

class MessageSubscriberSpec
    extends TestKit(
      ActorSystem(
        "message-subscriber",
        config
      )
    )
    with AnyWordSpecLike
    with Matchers
    with ImplicitSender
    with ScalaFutures
    with StopSystemAfterAll {

  val roomId        = RoomId(2L)
  val fromAccountId = AccountId(3L)
  val toAccountId   = AccountId(4L)

  override protected def afterAll(): Unit = {
    val redisClient = new RedisClient(config.getString("redis.host"), config.getInt("redis.port"))
    redisClient.del(readModelKey(toAccountId))

    super.afterAll()
  }

  "subscriber" when {
    "got ConsumedMessage" should {
      import io.circe.generic.auto._
      import io.circe.syntax._

      val message = Message(
        id = MessageId("1"),
        roomId = roomId,
        fromAccountId = fromAccountId,
        toAccountId = toAccountId,
        body = MessageBody("hello"),
        sendTime = SendTime(1593593046),
        updateTime = UpdateTime(0)
      )

      val messageJsonString = message.asJson.toString

      // TODO refactor to pass IO client instead of RedisClient
      val streamRepository = new StreamRepository {
        override def publishToPushNotification(message: QueryMessage): Future[Try[Boolean]] = Future.successful(Success(true))

        override def publish(channel: String, message: String): Future[Try[Boolean]] = ???
        override def publishToWebhookFlow(message: Message): Future[Try[Boolean]] = ???
        override def subscribe(channel: String, consumer: StreamConsumer): Unit = ???
        override def subscribeWebhookFlow(consumer: StreamConsumer): Unit = ???
        override def subscribePushNotification(consumer: StreamConsumer): Unit = ???
      }

      "run update readmodel flow" in {
        implicit val ec: ExecutionContext = system.dispatcher

        val mentionRepository = new MentionRepositoryRedisImpl(
          new RedisClient(config.getString("redis.host"), config.getInt("redis.port"))
        )

        val subscriber =
          system.actorOf(
            MessageSubscriberWorker.props(authenticationRepository, streamRepository, mentionRepository, chatworkApiRepository),
            "subscriber"
          )
        subscriber ! ConsumedMessage(messageJsonString)

        // wait to make sure RMU completed
        expectNoMessage(1 seconds)

        /**
          * access to redis
          * resolve read data
          */
        val mentionList = mentionRepository.resolve(toAccountId).futureValue
        mentionList.list.length shouldBe 1
        mentionList.list.head.body shouldBe MessageBody("hello")
      }

      "store up to 200 items" in {
        implicit val ec: ExecutionContext = system.dispatcher

        val mentionRepository = new MentionRepositoryRedisImpl(
          new RedisClient(config.getString("redis.host"), config.getInt("redis.port"))
        )

        val queryMessage = QueryMessage(
          id = message.id,
          roomId = message.roomId,
          roomName = RoomName(""),
          roomIconUrl = RoomIconUrl(""),
          fromAccountId = message.fromAccountId,
          fromAccountName = AccountName(""),
          fromAccountAvatarUrl = FromAccountAvatarUrl(""),
          toAccountId = message.toAccountId,
          body = message.body,
          sendTime = message.sendTime,
          updateTime = message.updateTime
        )

        val list = (0 to 200).foldLeft(Seq.empty[QueryMessage])((list, _) => list :+ queryMessage)
        val storeMentionList = MentionList(list)

        // save 200 items. and then consume 1 message then returns only 200 items
        mentionRepository.updateReadModel(toAccountId, storeMentionList)

        val subscriber = system.actorOf(MessageSubscriberWorker.props(authenticationRepository, streamRepository, mentionRepository, chatworkApiRepository), "subscriber_for_up_to")
        subscriber ! ConsumedMessage(messageJsonString)

        expectNoMessage(1000 milliseconds)

        val mentionList = mentionRepository.resolve(toAccountId).futureValue(Timeout(Span(10, Seconds)))
        mentionList.list.length shouldBe 200
      }
    }
  }

  def authenticationRepository(implicit ec: ExecutionContext): AuthenticationRepository = new AuthenticationRepository {
    override def resolve(accountId: AccountId): Future[Either[Throwable, Authentication]] = Future {
      Right(Authentication(toAccountId, ApiToken("token"), AccessToken("auth_token")))
    }

    override def issueAccessToken(authentication: Authentication): Future[Try[AccessToken]] = ???
  }

  def chatworkApiRepository(implicit ec: ExecutionContext): ChatworkApiRepository = new ChatworkApiRepository {

    override def retrieveRoom(roomId: RoomId)(implicit apiToken: ApiToken): Future[Room] = Future {
      Room(roomId, RoomName(""), RoomIconUrl(""))
    }

    override def retrieveAccount(roomId: RoomId, accountId: AccountId)(
        implicit apiToken: ApiToken
    ): Future[FromAccount] = Future { FromAccount(fromAccountId, AccountName(""), FromAccountAvatarUrl("")) }

    // no need
    override def me(implicit apiToken: ApiToken): Future[Me]                                           = ???
    override def resolveAccount(accountId: AccountId)(implicit apiToken: ApiToken): Future[MeResponse] = ???
  }

  // FIXME copied from repository...
  private def readModelKey(accountId: AccountId): String = {
    val md  = java.security.MessageDigest.getInstance("SHA-1")
    val key = s"read-model-${accountId.value}"
    md.digest(key.getBytes("UTF-8")).map("%02x".format(_)).mkString
  }
}
