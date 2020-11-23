package com.pays.market.api.service.user

import cats.effect.IO
import com.pays.market.api.UnitSpec
import com.pays.market.api.model.MarketApiException
import scalikejdbc.NoSession

class UserAlgebraSpec extends UnitSpec {

  "UserAlgebra" >> {
    "return an existing user by email" in {
      for {
        existingUser <- UserRepo.algebra(UserRepo.pureUser, UserRepo.pureUser).findByEmail("find")(NoSession)
      } yield existingUser should beEqualTo(UserRepo.user)
    }
    "return an existing user by id" in {
      for {
        existingUser <- UserRepo.algebra(UserRepo.pureUser, UserRepo.pureUser).findById(10)(NoSession)
      } yield existingUser should beEqualTo(UserRepo.user)
    }
    "create a new user" in {
      val create = User.Create(
        name = "name",
        email = "email"
      )
      for {
        userId <- UserRepo.algebra(UserRepo.pureUser, UserRepo.pureUser).create(create)(NoSession)
      } yield userId should beEqualTo(1000L)
    }
    "return NotFound exception in case of user does not exist" in {
      val error   = MarketApiException.notFound("not found")
      val input   = IO.raiseError(error)
      val attempt = UserRepo.algebra(input, input).findByEmail("find")(NoSession).attempt.unsafeRunSync()
      attempt must beLeft
      attempt.left.get must beEqualTo(error)
    }
    "return Exception in case of any other error" in {
      val error   = new Exception("random error")
      val input   = IO.raiseError(error)
      val attempt = UserRepo.algebra(input, input).findByEmail("find")(NoSession).attempt.unsafeRunSync()
      attempt must beLeft
      attempt.left.get must beEqualTo(error)
    }
  }
}
