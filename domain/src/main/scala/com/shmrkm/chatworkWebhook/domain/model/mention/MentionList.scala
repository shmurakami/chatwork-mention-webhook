package com.shmrkm.chatworkWebhook.domain.model.mention

case class MentionList(list: Seq[MentionMessage]) {
  def add(mentionMessage: MentionMessage) = new MentionList(Seq(mentionMessage) ++ list)
}
