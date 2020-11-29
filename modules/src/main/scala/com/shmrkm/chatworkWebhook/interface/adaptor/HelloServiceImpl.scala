package com.shmrkm.chatworkWebhook.interface.adaptor

import scala.concurrent.{ ExecutionContext, Future }

class HelloServiceImpl(implicit ec: ExecutionContext) extends HelloService {

  override def hello(in: HelloRequest): Future[HelloReply] = Future {
    HelloReply(s"Hello ${in.name}!!")
  }
}
