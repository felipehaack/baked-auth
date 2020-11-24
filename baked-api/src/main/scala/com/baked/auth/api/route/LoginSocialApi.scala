package com.baked.auth.api.route

import cats.effect.Async
import com.baked.auth.api.service.login.LoginService
import com.baked.auth.api.service.social.google.GoogleService
import com.baked.auth.api.service.social.model.Login
import org.http4s.HttpRoutes

trait LoginSocialApi[F[_]] extends ApiRoute[F] with Api[F]

object LoginSocialApi {

  def instance[F[_] : Async](
    googleService: GoogleService[F],
    loginService: LoginService[F]
  ): LoginSocialApi[F] =
    new LoginSocialApi[F] {
      override def routes: HttpRoutes[F] =
        HttpRoutes.of[F] {
          case r @ POST -> Root / "api" / "login" / "google" =>
            val result = for {
              tokenGoogle <- r.decodeJson[Login.WithGoogle]
              me          <- googleService.getMe(tokenGoogle)
              token       <- loginService.createSocial(me)
            } yield token
            result.asJson
        }
    }
}
