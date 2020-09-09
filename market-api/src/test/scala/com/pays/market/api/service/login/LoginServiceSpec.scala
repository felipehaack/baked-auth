package com.pays.market.api.service.login

import cats.effect.IO
import com.pays.market.api.UnitSpec
import com.pays.market.api.model.MarketApiException
import com.pays.market.api.service.password.UserPasswordService
import com.pays.market.api.service.user.UserService

class LoginServiceSpec extends UnitSpec {

  private val userService = UserService.instance[IO](
    db = noSessionPostgresDb,
    userAlgebra = UserRepo.algebra(IO.pure(UserRepo.user))
  )

  private val userPasswordService = UserPasswordService.instance[IO](
    db = noSessionPostgresDb,
    userPasswordAlgebra = UserPasswordRepo.algebra(IO.pure(UserPasswordRepo.userPassword))
  )

  private val loginService = LoginService.instance[IO](
    db = noSessionPostgresDb,
    jwtCodec = jwtCodec,
    userService = userService,
    userPasswordService = userPasswordService
  )

  private val login = Login.apply(
    email = "email@email.com",
    password = "password"
  )

  "LoginService" >> {
    "return a valid token" in {
      for {
        t <- loginService.create(login)
        _ = t.token.length must be_>(0)
        either <- jwtCodec.isValid(t.token).attempt
      } yield either must beRight
    }
    "return user not found" in {
      val error = MarketApiException.notFound("user")
      val localUserService = UserService.instance[IO](
        db = noSessionPostgresDb,
        userAlgebra = UserRepo.algebra(IO.raiseError(error))
      )

      val localLoginService = LoginService.instance[IO](
        db = noSessionPostgresDb,
        jwtCodec = jwtCodec,
        userService = localUserService,
        userPasswordService = userPasswordService
      )

      for {
        either <- localLoginService.create(login).attempt
      } yield {
        either must beLeft
        either.left.get must beEqualTo(MarketApiException.invalid("user_or_password"))
      }
    }
    "return password not found" in {
      val error = MarketApiException.notFound("password")
      val localUserPasswordService = UserPasswordService.instance[IO](
        db = noSessionPostgresDb,
        userPasswordAlgebra = UserPasswordRepo.algebra(IO.raiseError(error))
      )

      val localLoginService = LoginService.instance[IO](
        db = noSessionPostgresDb,
        jwtCodec = jwtCodec,
        userService = userService,
        userPasswordService = localUserPasswordService
      )

      for {
        either <- localLoginService.create(login).attempt
      } yield {
        either must beLeft
        either.left.get must beEqualTo(MarketApiException.invalid("user_or_password"))
      }
    }
    "return invalid password" in {
      val localLogin = login.copy(
        password = "invalid_password"
      )
      loginService.create(localLogin).attempt.map { either =>
        either must beLeft
        either.left.get must beEqualTo(MarketApiException.invalid("user_or_password"))
      }
    }
  }
}
