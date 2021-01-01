package com.shmrkm.chatworkMention.repository

import com.shmrkm.chatworkWebhook.domain.model.account.AccountId
import com.shmrkm.chatworkWebhook.domain.model.mention.MentionList

import scala.concurrent.Future
import scala.util.Try

trait MentionRepository {
  def resolve(accountId: AccountId): Future[MentionList]
  def fetch(accountId: AccountId): Future[MentionList]

  def updateReadModel(toAccountId: AccountId, mentionList: MentionList): Try[Any]
}
