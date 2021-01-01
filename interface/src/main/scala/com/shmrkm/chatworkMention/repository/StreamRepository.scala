package com.shmrkm.chatworkMention.repository

import com.shmrkm.chatworkWebhook.domain.model.message.Message
import com.shmrkm.chatworkWebhook.domain.model.query.message.QueryMessage

import scala.concurrent.Future
import scala.util.Try

trait StreamConsumer {
  def onSubscribe(channel: String): Unit
  def onUnsubscribe(channel: String): Unit
  def onMessage(message: String): Unit
  def onError(e: Throwable): Unit
}

trait StreamRepository {
  def publish(channel: String, message: Message): Future[Try[Boolean]]
  def publishToWebhookFlow(message: Message): Future[Try[Boolean]]
  def publishToPushNotification(message: QueryMessage): Future[Try[Boolean]]

  def subscribe(channel: String, consumer: StreamConsumer): Unit
  def subscribeWebhookFlow(consumer: StreamConsumer): Unit
  def subscribePushNotification(consumer: StreamConsumer): Unit
}
