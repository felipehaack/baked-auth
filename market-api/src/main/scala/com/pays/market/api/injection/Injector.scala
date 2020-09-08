package com.pays.market.api.injection

import cats.effect.Async
import com.pays.market.api.db.PostgresDb
import com.pays.market.api.route.{ ApiRoute, LoginApi }
import com.pays.market.api.service.login.LoginService
import com.pays.market.api.service.password.{ UserPassword, UserPasswordAlgebra, UserPasswordService }
import com.pays.market.api.service.user.{ User, UserAlgebra, UserService }
import com.pays.market.api.util.JwtCodec

trait Injector[F[_]] {
  val userAlgebra: UserAlgebra[F, User]
  val userService: UserService[F]
  val userPasswordAlgebra: UserPasswordAlgebra[F, UserPassword]
  val userPasswordService: UserPasswordService[F]
  val loginService: LoginService[F]
  val loginApi: LoginApi[F]
  val apis: List[ApiRoute[F]]
}

object Injector {

  def singleton[F[_] : Async](
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

      override val apis: List[ApiRoute[F]] = List(
        loginApi
      )
    }
}
