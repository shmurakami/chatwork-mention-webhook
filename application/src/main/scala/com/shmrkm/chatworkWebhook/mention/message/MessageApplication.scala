package com.shmrkm.chatworkWebhook.mention.message

import com.shmrkm.chatworkMention.repository.ChatworkApiRepository
import com.shmrkm.chatworkWebhook.domain.model.MessageId
import com.shmrkm.chatworkWebhook.domain.model.room.RoomId

import scala.concurrent.ExecutionContext

class MessageApplication(chatworkApiRepository: ChatworkApiRepository)(implicit ec: ExecutionContext) {
  def messageCreateFlow(roomId: RoomId, messageId: MessageId): Unit = {
    chatworkApiRepository.resolveAccount(roomId, messageId).map { accountName =>

    }
  }
}
