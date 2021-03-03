package com.shmrkm.chatworkWebhook.concerns

import com.shmrkm.chatworkMention.repository.{
  AuthenticationRepository,
  ChatworkApiRepository,
  MeResponse,
  MentionRepository,
  StreamConsumer,
  StreamRepository
}
import com.shmrkm.chatworkWebhook.domain.model.account.{ AccountId, FromAccount }
import com.shmrkm.chatworkWebhook.domain.model.auth.{ AccessToken, Authentication }
import com.shmrkm.chatworkWebhook.domain.model.chatwork.{ ApiToken, Me }
import com.shmrkm.chatworkWebhook.domain.model.mention.MentionList
import com.shmrkm.chatworkWebhook.domain.model.message.Message
import com.shmrkm.chatworkWebhook.domain.model.query.message.QueryMessage
import com.shmrkm.chatworkWebhook.domain.model.room.{ Room, RoomId }

import scala.concurrent.Future
import scala.util.Try

trait TestAuthenticationRepository extends AuthenticationRepository {
  override def resolve(accountId: AccountId): Future[Either[Throwable, Authentication]]   = ???
  override def issueAccessToken(authentication: Authentication): Future[Try[AccessToken]] = ???
}

trait TestStreamRepository extends StreamRepository {
  override def publish(channel: String, message: String): Future[Try[Boolean]]        = ???
  override def publishToWebhookFlow(message: Message): Future[Try[Boolean]]           = ???
  override def publishToPushNotification(message: QueryMessage): Future[Try[Boolean]] = ???
  override def subscribe(channel: String, consumer: StreamConsumer): Unit             = ???
  override def subscribeWebhookFlow(consumer: StreamConsumer): Unit                   = ???
  override def subscribePushNotification(consumer: StreamConsumer): Unit              = ???
}

trait TestMentionRepository extends MentionRepository {
  override def resolve(accountId: AccountId): Future[MentionList]                          = ???
  override def fetch(accountId: AccountId): Future[MentionList]                            = ???
  override def updateReadModel(toAccountId: AccountId, mentionList: MentionList): Try[Any] = ???
}

trait TestChatworkApiRepository extends ChatworkApiRepository {
  override def resolveAccount(accountId: AccountId)(implicit apiToken: ApiToken): Future[MeResponse] = ???
  override def retrieveRoom(roomId: RoomId)(implicit apiToken: ApiToken): Future[Room]               = ???

  override def retrieveAccount(roomId: RoomId, accountId: AccountId)(implicit apiToken: ApiToken): Future[FromAccount] =
    ???
  override def me(implicit apiToken: ApiToken): Future[Me] = ???
}
