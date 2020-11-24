package com.baked.auth.api.injection

import cats.effect.{ Async, ConcurrentEffect, ContextShift }
import com.baked.auth.api.config.{ AppConfig, EnvConfig }
import com.baked.auth.api.db.PostgresDb
import com.baked.auth.api.route.{ ApiRoute, LoginApi, LoginSocialApi }
import com.baked.auth.api.service.login.LoginService
import com.baked.auth.api.service.password.{ UserPassword, UserPasswordAlgebra, UserPasswordService }
import com.baked.auth.api.service.social.google.GoogleService
import com.baked.auth.api.service.user.{ User, UserAlgebra, UserService }
import com.baked.auth.api.util.{ Cors, JwtCodec }
import org.http4s.server.middleware.CORSConfig

trait Injector[F[_]] {
  val userAlgebra: UserAlgebra[F, User]
  val userService: UserService[F]
  val userPasswordAlgebra: UserPasswordAlgebra[F, UserPassword]
  val userPasswordService: UserPasswordService[F]
  val loginService: LoginService[F]
  val loginApi: LoginApi[F]
  val googleService: GoogleService[F]
  val loginSocialApi: LoginSocialApi[F]
  val apis: List[ApiRoute[F]]
  val cors: CORSConfig
}

object Injector {

  def singleton[F[_] : Async : ConcurrentEffect : ContextShift](
    appConfig: AppConfig,
    db: PostgresDb[F],
    jwtCodec: JwtCodec[F]
  ): Injector[F] =
    new Injector[F] {
      override val userAlgebra: UserAlgebra[F, User] =
        UserAlgebra.instance[F]
      override val userService: UserService[F] =
        UserService.instance[F](db, userAlgebra)

      override val userPasswordAlgebra: UserPasswordAlgebra[F, UserPassword] =
        UserPasswordAlgebra.instance[F]
      override val userPasswordService: UserPasswordService[F] =
        UserPasswordService.instance[F](db, userPasswordAlgebra)

      override val loginService: LoginService[F] =
        LoginService.instance(db, jwtCodec, userService, userPasswordService)
      override val loginApi: LoginApi[F] =
        LoginApi.instance[F](loginService)

      override val googleService: GoogleService[F] =
        if (appConfig.env != EnvConfig.Local) GoogleService.instance[F](appConfig.social.google)
        else GoogleService.localInstance[F]

      override val loginSocialApi: LoginSocialApi[F] =
        LoginSocialApi.instance[F](
          googleService = googleService,
          loginService = loginService
        )

      override val apis: List[ApiRoute[F]] = List(
        loginApi,
        loginSocialApi
      )

      override val cors: CORSConfig =
        Cors.cors(appConfig.cors)
    }
}
