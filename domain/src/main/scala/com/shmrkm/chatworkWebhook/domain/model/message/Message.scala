package com.shmrkm.chatworkWebhook.domain.model.message

case class Message(id: MessageId, body: MessageBody, sendTime: SendTime, updateTime: UpdateTime)
