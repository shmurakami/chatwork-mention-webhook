package com.shmrkm.chatworkWebhook.client

import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.grpc.GrpcClientSettings
import com.shmrkm.chatworkWebhook.interface.adaptor._

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

object Main extends App {

  implicit val system: ActorSystem          = ActorSystem("grpc-client")
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  val config = system.settings.config.getConfig("client")

  val grpcClientSettings = GrpcClientSettings
    .connectToServiceAt("127.0.0.1", 9090)
    .withTls(false)
    .withChannelBuilderOverrides(
      _.keepAliveWithoutCalls(true)
        .keepAliveTime(60L, TimeUnit.SECONDS)
    )

  //  auth()

//  list()
  subscribe()

  def auth(): Unit = {
    val client = AuthenticationServiceClient(grpcClientSettings)

    val accountId  = config.getInt("accountId")
    val cwApiToken = config.getString("chatworkApiToken")
    client
      .auth(AuthenticationRequest(accountId, cwApiToken))
      .onComplete {
        case Success(reply) => println(reply)
        case Failure(ex)    => println(ex)
      }
  }

  def hello(): Unit = {
    val client = HelloServiceClient(grpcClientSettings)

    client.hello(HelloRequest("Alice")).onComplete {
      case Success(reply) => println(reply.message)
      case Failure(ex)    => println("failure to say hello", ex)
    }
  }

  def list(): Unit = {
    val client = MentionServiceClient(grpcClientSettings)

    client
      .list(MentionListRequest(1))
      .onComplete {
        case Success(reply) => println(reply)
        case _              => println("error")
      }
  }

  def subscribe(): Unit = {
    val client = MentionSubscribeServiceClient(grpcClientSettings)

    println("start subscribe", LocalDateTime.now())
    client
      .subscribe(MentionSubscribeRequest(1))
      .runForeach(reply => {
        println(reply)
      })
      .recover {
        case e => println(s"error caused by : $e", LocalDateTime.now())
      }
  }

}
