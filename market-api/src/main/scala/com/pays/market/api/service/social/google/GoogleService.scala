package com.pays.market.api.service.social.google

import cats.FlatMap.ops._
import cats.effect.{ ContextShift, Sync }
import com.google.api.client.googleapis.auth.oauth2.{ GoogleIdToken, GoogleIdTokenVerifier }
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.pays.market.api.config.SocialGoogleConfig
import com.pays.market.api.model.MarketApiException
import com.pays.market.api.service.social.SocialService
import com.pays.market.api.service.social.model.Login

import scala.collection.JavaConverters._

trait GoogleService[F[_]] extends SocialService[F, Login.WithGoogle]

object GoogleService {

  def localInstance[F[_]](
    implicit S: Sync[F]
  ): GoogleService[F] =
    new GoogleService[F] {
      override def getMe(token: Login.WithGoogle): F[Login.Me] =
        S.pure {
          Login.Me(
            name = token.token,
            email = token.token
          )
        }
    }

  def instance[F[_]](
    config: SocialGoogleConfig
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
        token: Login.WithGoogle
      ): F[Login.Me] =
        C.shift *> S
              .delay {
                googleVerifier.verify(token.token)
              }
              .flatMap { userDetails =>
                Option
                  .apply(userDetails)
                  .map(_.getPayload)
                  .map(S.pure)
                  .getOrElse {
                    val error = MarketApiException.invalid("google_login")
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
