package com.shmrkm.chatworkWebhook.mention.message

import akka.actor.{Actor, Props}
import com.shmrkm.chatworkMention.repository.ChatworkApiRepository
import com.shmrkm.chatworkWebhook.domain.model.{MessageId, RoomId}

// TODO where is good to place protocol class?
case class MessageCreated(roomId: RoomId, messageId: MessageId)

object MessageActor {
  def props(messageApplication: MessageApplication): Props = Props(new MessageActor(messageApplication))
}

class MessageActor(messageApplication: MessageApplication) extends Actor {
  override def receive: Receive = {
    case MessageCreated(roomId, messageId) => {
      messageApplication.messageCreateFlow(roomId, messageId)
    }
  }
}
