package com.shmrkm.chatworkMention.repository
import com.redis.RedisClient
import com.shmrkm.chatworkMention.exception.StoreException
import com.shmrkm.chatworkMention.hash.{HashHelper, TokenGenerator}
import com.shmrkm.chatworkWebhook.domain.model.account.AccountId
import com.shmrkm.chatworkWebhook.domain.model.auth.{AccessToken, Authentication}

import scala.concurrent.{ExecutionContext, Future}

class AuthenticationRepositoryImpl(redisClient: RedisClient)(implicit ex: ExecutionContext) extends AuthenticationRepository with HashHelper {
  import io.circe.generic.auto._
  import io.circe.syntax._

  override def resolve(accessToken: AccessToken): Option[AccountId] = ???

  override def issueAccessToken(accountId: AccountId): Future[AccessToken] = {
    val tokenGenerator = new TokenGenerator
    val accessToken = AccessToken(tokenGenerator.generateSHAToken("authentication-"))
    Future {
      if (redisClient.set(authKey(accountId), Authentication(accountId, accessToken).asJson.toString)) accessToken
      else throw new StoreException("failed to store auth info")
    }
  }

  private def authKey(accountId: AccountId): String =
    sha1(s"authentication-${accountId.value}")
}
