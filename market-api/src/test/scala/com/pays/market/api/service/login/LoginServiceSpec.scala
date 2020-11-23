package com.pays.market.api.service.login

import cats.effect.IO
import com.pays.market.api.UnitSpec
import com.pays.market.api.model.MarketApiException
import com.pays.market.api.service.password.UserPasswordService
import com.pays.market.api.service.social.model.Login.Me
import com.pays.market.api.service.user.UserService

class LoginServiceSpec extends UnitSpec {

  private val userService = UserService.instance[IO](
    db = noSessionPostgresDb,
    userAlgebra = UserRepo.algebra(UserRepo.pureUser, UserRepo.pureUser)
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

  private val me = Me.apply(
    name = "name",
    email = "email@email.com"
  )

  "LoginService" >> {
    "normal login" >> {
      "return a valid token" in {
        for {
          t <- loginService.createNormal(login)
          _ = t.token.length must be_>(0)
          either <- jwtCodec.isValid(t.token).attempt
        } yield either must beRight
      }
      "return user not found" in {
        val error     = MarketApiException.notFound("user")
        val pureError = IO.raiseError(error)
        val localUserService = UserService.instance[IO](
          db = noSessionPostgresDb,
          userAlgebra = UserRepo.algebra(pureError, pureError)
        )

        val localLoginService = LoginService.instance[IO](
          db = noSessionPostgresDb,
          jwtCodec = jwtCodec,
          userService = localUserService,
          userPasswordService = userPasswordService
        )

        for {
          either <- localLoginService.createNormal(login).attempt
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
          either <- localLoginService.createNormal(login).attempt
        } yield {
          either must beLeft
          either.left.get must beEqualTo(MarketApiException.invalid("user_or_password"))
        }
      }
      "return invalid password" in {
        val localLogin = login.copy(
          password = "invalid_password"
        )
        loginService.createNormal(localLogin).attempt.map { either =>
          either must beLeft
          either.left.get must beEqualTo(MarketApiException.invalid("user_or_password"))
        }
      }
    }
    "social login" in {
      "get an instance of an existing user from the db based on the email" in {
        for {
          loginToken <- loginService.createSocial(me)
        } yield loginToken.token.nonEmpty must beTrue
      }
      "create a new user for a no existing user" in {
        val errorNotFound = MarketApiException.notFound("not found user")
        val localUser     = UserRepo.algebra(IO.raiseError(errorNotFound), IO.pure(UserRepo.user))
        val localUserService = UserService.instance[IO](
          db = noSessionPostgresDb,
          userAlgebra = localUser
        )
        val localLoginService = LoginService.instance[IO](
          db = noSessionPostgresDb,
          jwtCodec = jwtCodec,
          userService = localUserService,
          userPasswordService = userPasswordService
        )
        for {
          loginToken <- localLoginService.createSocial(me)
        } yield loginToken.token.nonEmpty must beTrue
      }
      "return an exception when trying to get an existing user" in {
        val exception     = MarketApiException.internalError("internal error")
        val pureException = IO.raiseError(exception)
        val localUser     = UserRepo.algebra(pureException, pureException)
        val localUserService = UserService.instance[IO](
          db = noSessionPostgresDb,
          userAlgebra = localUser
        )
        val localLoginService = LoginService.instance[IO](
          db = noSessionPostgresDb,
          jwtCodec = jwtCodec,
          userService = localUserService,
          userPasswordService = userPasswordService
        )
        localLoginService.createSocial(me).attempt.map { attempt =>
          attempt must beLeft
          attempt.left.get should beEqualTo(MarketApiException.invalid("user_or_password"))
        }
      }
    }
  }
}
