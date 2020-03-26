package com.shmrkm.chatworkWebhook.mention.controller

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.redis.RedisClient
import com.shmrkm.chatworkMention.repository.{ChatworkApiRepositoryImpl, MentionRepositoryRedisImpl}
import com.shmrkm.chatworkWebhook.domain.model.account.ToAccountId
import com.shmrkm.chatworkWebhook.domain.model.mention.MentionList
import com.shmrkm.chatworkWebhook.mention.protocol.MentionQuery

import scala.concurrent.{ExecutionContext, Future}

class MentionController(implicit system: ActorSystem) {
  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.generic.auto._

  private val chatworkApiConfig = system.settings.config.getConfig("chatwork.api")
  // don't want to put resource to api-server
  private val redisConfig = system.settings.config.getConfig("redis")

  def routes: Route =
    extractExecutionContext { implicit ec =>
      parameters('account_id) { accountId =>
        onSuccess(execute(MentionQuery(ToAccountId(accountId.toInt)))) { response =>
          complete(response)
        }
      }
    }

  def execute(query: MentionQuery)(implicit ec: ExecutionContext): Future[MentionList] = {
    val apiUrl = chatworkApiConfig.getString("url")
    val token = chatworkApiConfig.getString("token")
    val chatworkApiRepository = new ChatworkApiRepositoryImpl(apiUrl, token)
    val mentionRepository = new MentionRepositoryRedisImpl(new RedisClient(redisConfig.getString("host"), redisConfig.getInt("port")))

    for {
      // TODO calling api is not task for here. it's validation purpose
      me <- chatworkApiRepository.resolveAccount(query.accountId)
      mentionList <- mentionRepository.resolve(query.accountId)
    } yield mentionList
  }

}
