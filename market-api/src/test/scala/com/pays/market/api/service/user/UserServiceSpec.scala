package com.pays.market.api.service.user

import cats.effect.IO
import com.pays.market.api.UnitSpec
import com.pays.market.api.model.MarketApiException

class UserServiceSpec extends UnitSpec {

  private val findByEmail = "email@email.com"
  private val userAlgebra = UserRepo.algebra(UserRepo.pureUser, UserRepo.pureUser)

  private val userService = UserService.instance[IO](
    db = noSessionPostgresDb,
    userAlgebra = userAlgebra
  )

  "UserService" >> {
    "create a new user" in {
      val createUser = User.Create(
        name = "name",
        email = "email"
      )
      userService.create(createUser).map { userId =>
        userId must beEqualTo(1000L)
      }
    }
    "return an existing user by id" in {
      userService.getById(1000L).map { user =>
        user must beEqualTo(UserRepo.user)
      }
    }
    "return an existing user by email" in {
      userService.getByEmail(findByEmail).map { user =>
        user must beEqualTo(UserRepo.user)
      }
    }
    "return not found user" in {
      val error       = MarketApiException.notFound("not found user")
      val pureError   = IO.raiseError(error)
      val userAlgebra = UserRepo.algebra(pureError, pureError)
      val userService = UserService.instance[IO](
        db = noSessionPostgresDb,
        userAlgebra = userAlgebra
      )
      val either = userService.getByEmail(findByEmail).attempt.unsafeRunSync()
      either must beLeft
      either.left.get must beEqualTo(error)
    }
    "return Exception in case of any error" in {
      val error       = new Exception("random exception")
      val pureError   = IO.raiseError(error)
      val userAlgebra = UserRepo.algebra(pureError, pureError)
      val userService = UserService.instance[IO](
        db = noSessionPostgresDb,
        userAlgebra = userAlgebra
      )
      val either = userService.getByEmail(findByEmail).attempt.unsafeRunSync()
      either must beLeft
      either.left.get must beEqualTo(error)
    }
  }
}
