package com.shmrkm.chatworkWebhook.client

import akka.actor.ActorSystem
import akka.grpc.GrpcClientSettings
import com.shmrkm.chatworkWebhook.interface.adaptor.{AuthenticationRequest, AuthenticationServiceClient, HelloRequest, HelloServiceClient}

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

object Main extends App {

  implicit val system: ActorSystem          = ActorSystem("grpc-client")
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  val config = system.settings.config.getConfig("client")

  val grpcClientSettings = GrpcClientSettings.connectToServiceAt("127.0.0.1", 9090).withTls(false)

  auth()

  def auth(): Unit = {
    val client = AuthenticationServiceClient(grpcClientSettings)

    val accountId = config.getInt("accountId")
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
      case Failure(ex) => println("failure to say hello", ex)
    }
  }

}
