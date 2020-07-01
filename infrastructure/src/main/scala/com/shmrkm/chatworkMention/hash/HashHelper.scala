package com.shmrkm.chatworkMention.hash

private[chatworkMention] trait HashHelper {

  def sha1(key: String): String = {
    val md = java.security.MessageDigest.getInstance("SHA-1")
    md.digest(key.getBytes("UTF-8")).map("%02x".format(_)).mkString
  }

}
