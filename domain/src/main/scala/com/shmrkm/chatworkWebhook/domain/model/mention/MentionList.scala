package com.shmrkm.chatworkWebhook.domain.model.mention

import com.shmrkm.chatworkWebhook.domain.model.query.message.QueryMessage

case class MentionList(list: Seq[QueryMessage]) {
  def add(queryMessage: QueryMessage): MentionList = MentionList(Seq(queryMessage) ++ list)
}
