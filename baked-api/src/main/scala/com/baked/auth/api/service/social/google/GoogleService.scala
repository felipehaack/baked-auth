package com.baked.auth.api.service.social.google

import cats.FlatMap.ops._
import cats.effect.{ ContextShift, Sync }
import com.baked.auth.api.config.SocialAudienceConfig
import com.baked.auth.api.model.BakedAuthException
import com.baked.auth.api.service.social.SocialService
import com.baked.auth.api.service.social.model.Login
import com.google.api.client.googleapis.auth.oauth2.{ GoogleIdToken, GoogleIdTokenVerifier }
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory

import scala.collection.JavaConverters._

trait GoogleService[F[_]] extends SocialService[F]

object GoogleService {

  def localInstance[F[_]](
    implicit S: Sync[F]
  ): GoogleService[F] =
    new GoogleService[F] {
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
  ): GoogleService[F] =
    new GoogleService[F] {
      private val googleVerifier: GoogleIdTokenVerifier = {
        val net          = new NetHttpTransport()
        val factory      = new JacksonFactory()
        val tokeVerifier = new GoogleIdTokenVerifier.Builder(net, factory)
        val audiences    = List(config.audience)
        tokeVerifier.setAudience(audiences.asJavaCollection)
        tokeVerifier.build()
      }

      override def getMe(
        accessToken: Login.WithAccessToken
      ): F[Login.Me] =
        C.shift *> S
              .delay {
                googleVerifier.verify(accessToken.token)
              }
              .flatMap { userDetails =>
                Option
                  .apply(userDetails)
                  .map(_.getPayload)
                  .map(S.pure)
                  .getOrElse {
                    val error = BakedAuthException.invalid("google_login")
                    S.raiseError[GoogleIdToken.Payload](error)
                  }
              }
              .map { payload =>
                Login.Me(
                  name = payload.get("name").asInstanceOf[String],
                  email = payload.getEmail
                )
              }
    }
}
