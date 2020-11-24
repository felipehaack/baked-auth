package com.baked.auth.api.service.login

import cats.ApplicativeError
import cats.FlatMap.ops._
import cats.effect.Sync
import cats.syntax.ApplicativeErrorSyntax
import com.baked.auth.api.db.PostgresDb
import com.baked.auth.api.model.{ MarketApiException, MarketNotFoundException }
import com.baked.auth.api.service.password.UserPasswordService
import com.baked.auth.api.service.social.model.Login.Me
import com.baked.auth.api.service.user.{ User, UserService }
import com.baked.auth.api.util.{ JwtCodec, Log }
import com.github.t3hnar.bcrypt._

import scala.util.{ Failure, Success }

trait LoginService[F[_]] extends ApplicativeErrorSyntax with Log {
  def createNormal(login: Login): F[Login.Token]
  def createSocial(me: Me): F[Login.Token]
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
      override def createNormal(
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

        r.handleErrorWith(handleLoginError)
      }

      override def createSocial(me: Me): F[Login.Token] = {
        val r = for {
          user  <- getOrCreate(me)
          token <- jwtCodec.encode(user)
        } yield Login.Token.apply(token)

        r.handleErrorWith(handleLoginError)
      }

      private def getOrCreate(me: Me): F[User] = {
        def create(me: Me): F[User] = {
          val createUser = User.Create(
            name = me.name,
            email = me.email
          )
          for {
            userId <- userService.create(createUser)
            user   <- userService.getById(userId)
          } yield user
        }
        userService
          .getByEmail(me.email)
          .handleErrorWith {
            case _: MarketNotFoundException => create(me)
            case e                          => A.raiseError(e)
          }
      }

      private def handleLoginError(t: Throwable): F[Login.Token] = {
        logger.info("login failed with following error", t)
        A.raiseError[Login.Token](MarketApiException.invalid("user_or_password"))
      }
    }
}
