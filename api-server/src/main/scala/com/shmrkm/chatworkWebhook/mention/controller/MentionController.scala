package com.shmrkm.chatworkWebhook.mention.controller

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.redis.RedisClient
import com.shmrkm.chatworkMention.exception.{InvalidAccountIdException, RequestFailureException}
import com.shmrkm.chatworkMention.repository.{ChatworkApiRepositoryImpl, MentionRepositoryRedisImpl}
import com.shmrkm.chatworkWebhook.domain.model.account.AccountId
import com.shmrkm.chatworkWebhook.domain.model.mention.MentionList
import com.shmrkm.chatworkWebhook.mention.protocol.read.MentionErrorResponse.InvalidRequest
import com.shmrkm.chatworkWebhook.mention.protocol.read.MentionQuery
import com.typesafe.scalalogging.Logger

import scala.concurrent.{ExecutionContext, Future}

class MentionController(implicit system: ActorSystem) {

  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.generic.auto._

  val chatworkApiConfig = system.settings.config.getConfig("chatwork.api")
  val apiUrl            = chatworkApiConfig.getString("url")
  // don't want to put resource to api-server
  val redisConfig = system.settings.config.getConfig("redis")

  def routes: Route =
    extractExecutionContext { implicit ec =>
      headerValueByName("X-ChatworkToken") { chatworkToken =>
        parameters('account_id.as[Int]) { accountId =>
          onSuccess(execute(MentionQuery(AccountId(accountId)), chatworkToken)) {
            case Right(mentionList) => complete(mentionList)
            case Left(_)            => complete(StatusCodes.BadRequest, InvalidRequest())
          }
        }
      }
    }

  def execute(query: MentionQuery, token: String)(
      implicit ec: ExecutionContext
  ): Future[Either[String, MentionList]] = {
    // seems Either Left should be any type

    // validate token
    val chatworkApiRepository = new ChatworkApiRepositoryImpl(apiUrl, token)
    val mentionRepository = new MentionRepositoryRedisImpl(
      new RedisClient(redisConfig.getString("host"), redisConfig.getInt("port"))
    )

    val logger = Logger(classOf[MentionController])
    (for {
      - <- chatworkApiRepository.resolveAccount(query.accountId)
      mentionList <- mentionRepository.resolve(query.accountId)
    } yield Right(mentionList))
      .recover {
        case e: InvalidAccountIdException =>
          logger.warn(e.toString)
          Left("error")
        case e: RequestFailureException =>
          logger.warn(e.toString)
          Left("try again")
      }
  }

}
