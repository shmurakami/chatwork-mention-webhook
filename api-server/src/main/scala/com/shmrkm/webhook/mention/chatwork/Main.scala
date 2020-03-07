package com.shmrkm.webhook.mention.chatwork

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._

import scala.io.StdIn

object Main {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("webhook")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val route =
      path("hello") {
        get {
          complete(HttpEntity(ContentTypes.`application/json`, """{"success":true}"""))
        }
      }

    val host = "localhost"
    val port = 18080
    val bidingFuture = Http().bindAndHandle(route, host, port)

    println(s"Server online at http://${host}:${port}/\nPress RETURN to stop...")
    StdIn.readLine()
    bidingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())

  }
}
