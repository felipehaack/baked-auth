package com.baked.auth.api.service.social.facebook

import cats.FlatMap.ops._
import cats.effect.{ContextShift, Sync}
import com.baked.auth.api.typeclass.BakedAuthMonadError
import com.baked.auth.api.config.SocialAudienceConfig
import com.baked.auth.api.model.BakedAuthException
import com.baked.auth.api.service.social.SocialService
import com.baked.auth.api.service.social.model.Login
import com.restfb.types.User
import com.restfb.{DefaultFacebookClient, Parameter, Version}

trait FacebookService[F[_]] extends SocialService[F] with BakedAuthMonadError {
  val params: Parameter = Parameter.`with`("fields", "name,email")
  val version: Version  = Version.VERSION_9_0
}

object FacebookService {

  def localInstance[F[_]](implicit S: Sync[F]): FacebookService[F] =
    new FacebookService[F] {
      override def getMe(accessToken: Login.WithAccessToken): F[Login.Me] =
        S.pure {
          Login.Me(
            name = accessToken.token,
            email = accessToken.token
          )
        }
    }

  def instance[F[_]](
    config: SocialAudienceConfig
  )(
    implicit S: Sync[F],
    C: ContextShift[F]
  ): FacebookService[F] =
    new FacebookService[F] {
      override def getMe(accessToken: Login.WithAccessToken): F[Login.Me] =
        C.shift *> {
          for {
            client <-
              S.delay {
                  new DefaultFacebookClient(accessToken.token, config.audience, version)
                }
                .logThrow { e =>
                  logger.info("failed to define a facebook client", e.getMessage)
                } { _ =>
                  BakedAuthException.invalid("invalid_access_token")
                }
            user <-
              S.delay {
                  client.fetchObject("me", classOf[User], params)
                }
                .logThrow { e =>
                  logger.info("failed to collect the facebook user details", e.getMessage)
                } { _ =>
                  BakedAuthException.invalid("invalid_user_details")
                }
          } yield Login.Me(
            name = user.getName,
            email = user.getEmail
          )
        }
    }
}
