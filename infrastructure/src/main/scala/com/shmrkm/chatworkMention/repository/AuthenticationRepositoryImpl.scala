package com.shmrkm.chatworkMention.repository

import com.redis.RedisClient
import com.shmrkm.chatworkMention.exception.StoreException
import com.shmrkm.chatworkMention.hash.{HashHelper, TokenGenerator}
import com.shmrkm.chatworkWebhook.domain.model.account.AccountId
import com.shmrkm.chatworkWebhook.domain.model.auth.{AccessToken, Authentication}

import scala.concurrent.{ExecutionContext, Future}

class AuthenticationRepositoryImpl(redisClient: RedisClient)(implicit ex: ExecutionContext)
    extends AuthenticationRepository
    with HashHelper {
  import io.circe.generic.auto._
  import io.circe.parser._
  import io.circe.syntax._

  override def resolve(accessToken: AccessToken): Future[Option[Authentication]] = {
    Future {
      redisClient.get(accessToken.value) match {
        // TODO left
        case Some(value: String) =>
          parse(value).right.flatMap(_.as[Authentication]) match {
            case Right(authentication) => Some(authentication)
            case Left(_)               => None
          }
        case None => None
      }
    }
  }

  override def issueAccessToken(authentication: Authentication): Future[AccessToken] = {
    Future {
      val accessToken = authKey(authentication.accountId)
      if (redisClient.set(accessToken.value, authentication.asJson.toString)) accessToken
      else throw new StoreException("failed to store auth info")
    }
  }

  private def authKey(accountId: AccountId): AccessToken = {
    val tokenGenerator = new TokenGenerator
    AccessToken(tokenGenerator.generateSHAToken(s"authentication-${accountId.value}"))
  }
}
