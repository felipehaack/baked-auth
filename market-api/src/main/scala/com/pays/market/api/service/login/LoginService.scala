package com.pays.market.api.service.login

import cats.ApplicativeError
import cats.FlatMap.ops._
import cats.effect.Sync
import cats.syntax.ApplicativeErrorSyntax
import com.github.t3hnar.bcrypt._
import com.pays.market.api.db.PostgresDb
import com.pays.market.api.model.MarketApiException
import com.pays.market.api.service.password.UserPasswordService
import com.pays.market.api.service.user.UserService
import com.pays.market.api.util.{ JwtCodec, Log }

import scala.util.{ Failure, Success }

trait LoginService[F[_]] extends ApplicativeErrorSyntax with Log {
  def create(login: Login): F[Login.Token]
}

object LoginService {

  def instance[F[_] : Sync](
    db: PostgresDb[F],
    jwtCodec: JwtCodec[F],
    userService: UserService[F],
    userPasswordService: UserPasswordService[F]
  )(
    implicit A: ApplicativeError[F, Throwable]
  ): LoginService[F] =
    new LoginService[F] {
      override def create(
        login: Login
      ): F[Login.Token] = {
        val r = for {
          user         <- userService.getByEmail(login.email)
          userPassword <- userPasswordService.get(user.id)
          _ <- login.password.isBcryptedSafe(userPassword.encryptedPassword) match {
            case Success(v) if v  => A.unit
            case Success(v) if !v => A.raiseError[Unit](MarketApiException.invalid("password_invalid"))
            case Failure(e) =>
              logger.info(s"unexpected password error for $login", e)
              A.raiseError[Unit](MarketApiException.invalid("password_decrypt_invalid"))
          }
          token <- jwtCodec.encode(user)
        } yield Login.Token.apply(token)

        r.handleErrorWith { e =>
          logger.info("login failed with following error", e)
          A.raiseError[Login.Token](MarketApiException.invalid("user_or_password"))
        }
      }
    }
}
