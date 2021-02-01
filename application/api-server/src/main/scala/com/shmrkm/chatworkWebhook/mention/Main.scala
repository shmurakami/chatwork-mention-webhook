package com.shmrkm.chatworkWebhook.mention

import java.util.concurrent.TimeUnit

import akka.Done
import akka.actor.{ActorSystem, CoordinatedShutdown}
import akka.grpc.scaladsl.ServiceHandler
import akka.http.scaladsl.{Http, HttpConnectionContext}
import com.redis.RedisClient
import com.shmrkm.chatworkMention.repository.{AuthenticationRepositoryImpl, ChatworkApiRepositoryImpl, MentionRepositoryFactory, MentionRepositoryRedisImpl}
import com.shmrkm.chatworkWebhook.auth.AccessTokenGeneratorImpl
import com.shmrkm.chatworkWebhook.interface.adaptor.{AuthenticationServiceHandler, AuthenticationServiceImpl, HelloServiceHandler, HelloServiceImpl, MentionServiceHandler, MentionServiceImpl, MentionServicePowerApiHandler, MentionSubscribeServiceHandler, MentionSubscribeServiceImpl, MentionSubscribeServicePowerApiHandler}
import com.shmrkm.chatworkWebhook.mention.controller.{AuthenticationController, MentionController, WebhookController}
import com.shmrkm.chatworkWebhook.mention.usecase.MentionListUseCaseImpl
import com.typesafe.config.ConfigFactory

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}
import scala.concurrent.duration.FiniteDuration

object Main extends MentionRepositoryFactory {
//  Kamon.init()
  val config = ConfigFactory.load()

  implicit val system: ActorSystem = ActorSystem("mention-webhook", config)
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  override implicit def ec: ExecutionContext = executionContext

  def main(args: Array[String]): Unit = {

    val mentionListUseCase = new MentionListUseCaseImpl(factoryMentionRepository())

    // TODO use airframe
    val routes = new Routes(
      new AuthenticationController,
      new WebhookController,
      new MentionController(mentionListUseCase)
    )

    val host          = config.getString("chatwork-mention-webhook.api-server.host")
    val port          = config.getInt("chatwork-mention-webhook.api-server.port")
    val bindingFuture = Http().bindAndHandle(routes.routes, host, port)
    bindingFuture.foreach(_ => println(s"bind http server as ${host}:${port}"))

    val grpcInterface = config.getString("chatwork-mention-webhook.grpc-server.host")
    val grpcPort = config.getInt("chatwork-mention-webhook.grpc-server.port")

    val authenticationRepository = new AuthenticationRepositoryImpl(new RedisClient(config.getString("redis.host"), config.getInt("redis.port")))

    val authenticationService = AuthenticationServiceHandler.partial(
      AuthenticationServiceImpl(
        new AccessTokenGeneratorImpl(),
        new ChatworkApiRepositoryImpl(config.getString("chatwork.api.url")),
        authenticationRepository
      )
    )

    val mentionService = MentionServicePowerApiHandler.partial(new MentionServiceImpl(mentionListUseCase, authenticationRepository))
    val mentionSubscribeService = MentionSubscribeServicePowerApiHandler.partial(new MentionSubscribeServiceImpl(null, authenticationRepository))
    val helloService = HelloServiceHandler.partial(new HelloServiceImpl)
    val service        = ServiceHandler.concatOrNotFound(authenticationService, mentionService, mentionSubscribeService, helloService)
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
