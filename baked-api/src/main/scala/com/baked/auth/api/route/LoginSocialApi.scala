package com.baked.auth.api.route

import cats.effect.Async
import com.baked.auth.api.service.login.{ Login, LoginService }
import com.baked.auth.api.service.social.facebook.FacebookService
import com.baked.auth.api.service.social.google.GoogleService
import com.baked.auth.api.service.social.model.{ Login => SocialLogin }
import org.http4s.{ HttpRoutes, Request }

trait LoginSocialApi[F[_]] extends ApiRoute[F] with Api[F]

object LoginSocialApi {

  def instance[F[_] : Async](
    googleService: GoogleService[F],
    facebookService: FacebookService[F],
    loginService: LoginService[F]
  ): LoginSocialApi[F] =
    new LoginSocialApi[F] {
      private def processLogin(
        r: Request[F],
        f: SocialLogin.WithAccessToken => F[SocialLogin.Me]
      ): F[Login.Token] =
        for {
          accessToken <- r.decodeJson[SocialLogin.WithAccessToken]
          me          <- f.apply(accessToken)
          token       <- loginService.createSocial(me)
        } yield token

      override def routes: HttpRoutes[F] =
        HttpRoutes.of[F] {
          case r @ POST -> Root / "api" / "login" / "google" =>
            processLogin(r, googleService.getMe).asJson
          case r @ POST -> Root / "api" / "login" / "facebook" =>
            processLogin(r, facebookService.getMe).asJson
        }
    }
}
