package com.pays.market.api.util

import java.util.Date

import cats.Functor.ops._
import cats.MonadError
import cats.syntax.ApplicativeErrorSyntax
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.pays.market.api.config.JwtConfig
import com.pays.market.api.model.MarketApiException
import com.pays.market.api.service.user.User

trait JwtCodec[F[_]] extends ApplicativeErrorSyntax with Log {
  val userKey: String
  def encode(user: User): F[String]
  def isValid(token: String): F[Unit]
}

object JwtCodec {

  def instance[F[_]](
    jwtConfig: JwtConfig
  )(
    implicit M: MonadError[F, Throwable]
  ): JwtCodec[F] =
    new JwtCodec[F] {
      override val userKey = "user"

      override def encode(user: User): F[String] =
        M.catchNonFatal {
          JWT
            .create()
            .withClaim(userKey, user.email)
            .withExpiresAt(new Date(jwtConfig.expireInMilliseconds))
            .sign(Algorithm.HMAC256(jwtConfig.secret))
        }

      def isValid(token: String): F[Unit] =
        M.catchNonFatal {
            JWT
              .require(Algorithm.HMAC256(jwtConfig.secret))
              .build()
              .verify(token)
          }
          .map(_ => ())
          .handleErrorWith { e =>
            logger.info(s"invalid token with error ${e.getMessage}")
            M.raiseError(MarketApiException.invalid("token"))
          }
    }
}
