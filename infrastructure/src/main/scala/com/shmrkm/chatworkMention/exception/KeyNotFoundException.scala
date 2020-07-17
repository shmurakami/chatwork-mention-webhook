package com.shmrkm.chatworkMention.exception

case class KeyNotFoundException(key: Option[String] = None) extends Exception
