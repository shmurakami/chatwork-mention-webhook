package com.shmrkm.chatworkMention.repository

import com.shmrkm.chatworkWebhook.domain.model.account.{AccountId, FromAccount}
import com.shmrkm.chatworkWebhook.domain.model.chatwork.{ApiToken, Me}
import com.shmrkm.chatworkWebhook.domain.model.room.{Room, RoomId}

import scala.concurrent.Future

trait ChatworkApiRepository {

  def resolveAccount(accountId: AccountId)(implicit apiToken: ApiToken): Future[MeResponse]

  def retrieveRoom(roomId: RoomId)(implicit apiToken: ApiToken): Future[Room]

  def retrieveAccount(roomId: RoomId, accountId: AccountId)(implicit apiToken: ApiToken): Future[FromAccount]

  def me(implicit apiToken: ApiToken): Future[Me]
}
