package com.shmrkm.chatworkMention.repository

import com.redis.RedisClient
import com.shmrkm.chatworkMention.exception.{ InvalidValueException, KeyNotFoundException, StoreException }
import com.shmrkm.chatworkMention.hash.TokenGenerator
import com.shmrkm.chatworkWebhook.domain.model.account.AccountId
import com.shmrkm.chatworkWebhook.domain.model.auth.{ AccessToken, Authentication }
import com.typesafe.scalalogging.Logger

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success, Try }

class AuthenticationRepositoryImpl(redisClient: RedisClient)(implicit ex: ExecutionContext)
    extends AuthenticationRepository {
  import io.circe.generic.auto._
  import io.circe.parser._
  import io.circe.syntax._

  private val logger = Logger(classOf[AuthenticationRepository])

  override def resolve(accessToken: AccessToken): Future[Either[Throwable, Authentication]] = {
    Future {
      redisClient.get(accessToken.value) match {
        case Some(value: String) =>
          parse(value) match {
            case Left(error) =>
              logger.warn(s"failed to parse access token json due to $error")
              Left(InvalidValueException())
            case Right(json) => json.as[Authentication]
          }
        case None => Left(KeyNotFoundException())
      }
    }
  }

  override def issueAccessToken(authentication: Authentication): Future[Try[AccessToken]] = {
    Future {
      val accessToken = authKey(authentication.accountId)
      if (redisClient.set(accessToken.value, authentication.asJson.noSpaces)) Success(accessToken)
      else Failure(new StoreException("failed to store auth info"))
    }
  }

  private def authKey(accountId: AccountId): AccessToken = {
    val tokenGenerator = new TokenGenerator
    AccessToken(tokenGenerator.generateSHAToken(s"authentication-${accountId.value}"))
  }

  override def authenticationForAccountId(accountId: AccountId): Future[Either[Throwable, Authentication]] = {
    resolve(authKey(accountId))
  }
}
