package com.shmrkm.chatworkWebhook.mention

import java.util.concurrent.TimeUnit

import akka.Done
import akka.actor.{ActorSystem, CoordinatedShutdown}
import akka.grpc.scaladsl.ServiceHandler
import akka.http.scaladsl.{Http, HttpConnectionContext}
import com.redis.RedisClient
import com.shmrkm.chatworkMention.repository.{AuthenticationRepositoryImpl, ChatworkApiRepositoryImpl}
import com.shmrkm.chatworkWebhook.auth.AccessTokenGeneratorImpl
import com.shmrkm.chatworkWebhook.interface.adaptor.{AuthenticationServiceHandler, AuthenticationServiceImpl, HelloServiceHandler, HelloServiceImpl, MentionServiceHandler, MentionServiceImpl}
import com.shmrkm.chatworkWebhook.mention.controller.{AuthenticationController, MentionController, WebhookController}
import com.typesafe.config.ConfigFactory

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

object Main {
//  Kamon.init()

  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load()

    implicit val system           = ActorSystem("mention-webhook", config)
    implicit val executionContext = system.dispatcher

    // TODO use airframe
    val routes = new Routes(
      new AuthenticationController,
      new WebhookController,
      new MentionController
    )

    val host          = config.getString("chatwork-mention-webhook.api-server.host")
    val port          = config.getInt("chatwork-mention-webhook.api-server.port")
    val bindingFuture = Http().bindAndHandle(routes.routes, host, port)
    bindingFuture.foreach(_ => println(s"bind http server as ${host}:${port}"))

    val grpcInterface = config.getString("chatwork-mention-webhook.grpc-server.host")
    val grpcPort = config.getInt("chatwork-mention-webhook.grpc-server.port")

    val authenticationService = AuthenticationServiceHandler.partial(
      AuthenticationServiceImpl(
        new AccessTokenGeneratorImpl(),
        new ChatworkApiRepositoryImpl(config.getString("chatwork.api.url")),
        new AuthenticationRepositoryImpl(new RedisClient(config.getString("redis.host"), config.getInt("redis.port")))
      )
    )

    val mentionService = MentionServiceHandler.partial(new MentionServiceImpl)
    val helloService = HelloServiceHandler.partial(new HelloServiceImpl)
    val service        = ServiceHandler.concatOrNotFound(authenticationService, mentionService, helloService)
    val grpcBindingFuture = Http().bindAndHandleAsync(
      service,
      interface = grpcInterface,
      port = grpcPort,
      connectionContext = HttpConnectionContext()
    )
    grpcBindingFuture.foreach(_ => println(s"bind grpc server as ${grpcInterface}:${grpcPort}"))

    val terminationDuration = FiniteDuration(
      config.getDuration("chatwork-mention-webhook.termination-duration").toMillis,
      TimeUnit.MILLISECONDS
    )

    CoordinatedShutdown(system).addTask(CoordinatedShutdown.PhaseBeforeServiceUnbind, "shutdown http server") { () =>
      Future {
        bindingFuture.flatMap(_.terminate(terminationDuration))
        grpcBindingFuture.flatMap(_.terminate(terminationDuration))
        Done.done()
      }
    }
  }
}
