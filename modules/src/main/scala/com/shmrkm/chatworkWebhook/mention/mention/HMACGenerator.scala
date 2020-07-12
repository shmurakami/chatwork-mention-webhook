package com.shmrkm.chatworkWebhook.mention.mention

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object HMACGenerator {

  def generate(secretKey: Array[Byte], value: String): Array[Byte] = {
    val algorithm = "HmacSHA256"
    val secret = new SecretKeySpec(secretKey, algorithm)
    val mac = Mac.getInstance(algorithm)
    mac.init(secret)
    mac.doFinal(value.getBytes("UTF-8"))
  }

}
