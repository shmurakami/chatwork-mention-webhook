package com.shmrkm.chatworkWebhook.mention.controller

import java.util.Base64

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive, Directive0, Directive1, MalformedRequestContentRejection, Route}
import akka.pattern.ask
import akka.util.{ByteString, Timeout}
import com.shmrkm.chatworkWebhook.mention.mention.MentionHandlerActor.ReceiveMention
import com.shmrkm.chatworkWebhook.mention.mention.{HMACGenerator, MentionHandlerActor}
import com.shmrkm.chatworkWebhook.mention.protocol.command.WebhookResponse
import com.shmrkm.chatworkWebhook.mention.protocol.write.{MentionCommand, WebhookRequest}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class WebhookController(implicit system: ActorSystem) extends Controller {

  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.generic.auto._

  private val chatworkConfig = system.settings.config.getConfig("chatwork")
  private val signatureHeaderKey = chatworkConfig.getString("webhook.signature_header_key")

  private val hmacSecret = Base64.getDecoder.decode(chatworkConfig.getString("webhook.token"))

  // TODO directive1...
//  def extractRequestBody(entity: HttpEntity)(implicit ec: ExecutionContext): Directive1[Future[String]] = {
//    Tuple1(entity.dataBytes.runFold(ByteString.empty)(_ ++ _).map(_.utf8String))
//  }

  def verifySignature(expectSignature: String, body: String): Directive0 = {
    val hmac = HMACGenerator.generate(hmacSecret, body)
    val signature = Base64.getEncoder.encodeToString(hmac)
    if (expectSignature == signature) pass
    else reject(MalformedRequestContentRejection("Invalid Request", new RuntimeException("Invalid Request")))
  }

  implicit val timeout: Timeout =
    system.settings.config.getInt("chatwork-mention-webhook.api-server.timeout-seconds") seconds

  def route: Route =
    post {
      headerValueByName(signatureHeaderKey) { applySignature =>
        extractExecutionContext { implicit ec =>
          extractRequestEntity { requestEntity =>
            onSuccess(requestEntity.dataBytes.runFold(ByteString.empty)(_ ++ _).map(_.utf8String)) { body =>
              verifySignature(applySignature, body) {
                entity(as[WebhookRequest]) { request =>
                  onComplete(execute(request.mentionCommand)) { response =>
                    complete(response)
                  }
                }
              }
            }
          }
        }
      }
    }

  /**
    * store to queue
    */
  def execute(request: MentionCommand)(implicit ec: ExecutionContext): Future[WebhookResponse] = {
    system.actorOf(MentionHandlerActor.props).ask(ReceiveMention(request)).map(_ => WebhookResponse())
  }
}
