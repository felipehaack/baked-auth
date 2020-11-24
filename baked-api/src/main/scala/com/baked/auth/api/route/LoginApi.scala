package com.baked.auth.api.route

import cats.effect.Async
import com.baked.auth.api.service.login.{ Login, LoginService }
import org.http4s._

trait LoginApi[F[_]] extends ApiRoute[F] with Api[F]

object LoginApi {

  def instance[F[_] : Async](
    loginService: LoginService[F]
  ): LoginApi[F] =
    new LoginApi[F] {
      override def routes: HttpRoutes[F] =
        HttpRoutes.of[F] {
          case r @ POST -> Root / "api" / "login" =>
            val result = for {
              login <- r.decodeJson[Login]
              token <- loginService.createNormal(login)
            } yield token
            result.asJson
        }
    }
}
