package com.shmrkm.chatworkMention.repository

import com.shmrkm.chatworkWebhook.domain.model.account.{FromAccountId, ToAccountId}
import com.shmrkm.chatworkWebhook.domain.model.mention.MentionMessage
import com.shmrkm.chatworkWebhook.domain.model.message.Message
import com.shmrkm.chatworkWebhook.domain.model.room.RoomId

import scala.concurrent.Future

trait ChatworkApiRepository {
  def resolveMentionMessage(roomId: RoomId, fromAccountId: FromAccountId, message: Message): Future[Option[MentionMessage]]

  def resolveAccount(accountId: ToAccountId): Future[Option[MeResponse]]
}
