package com.shmrkm.chatworkWebhook.sample

import com.typesafe.config.ConfigFactory

class SampleModule {

  val config = ConfigFactory.load("sample")

  def show: String = {
    config.getString("root-key.key")
  }

}
