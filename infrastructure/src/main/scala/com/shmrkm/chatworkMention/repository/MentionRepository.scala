package com.shmrkm.chatworkMention.repository

import com.shmrkm.chatworkWebhook.domain.model.ToAccountId
import shapeless.Succ

import scala.concurrent.Future

// TODO domain
case class MentionMessage(value: Any)
case class MentionList(list: Seq[MentionMessage])

class MentionRepository {
  def store(list: MentionList): Future[Boolean] = ???
  def resolve(accountId: ToAccountId): Future[Some[MentionList]] = ???
}
