package com.shmrkm.chatworkMention.repository

import com.shmrkm.chatworkWebhook.domain.model.account.{AccountId, FromAccount}
import com.shmrkm.chatworkWebhook.domain.model.chatwork.Me
import com.shmrkm.chatworkWebhook.domain.model.room.{Room, RoomId}

import scala.concurrent.Future

trait ChatworkApiRepository {

  def resolveAccount(accountId: AccountId): Future[MeResponse]

  def retrieveRoom(roomId: RoomId): Future[Room]

  def retrieveAccount(roomId: RoomId, accountId: AccountId): Future[FromAccount]

  def me(): Future[Me]
}
