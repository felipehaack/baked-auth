package com.pays.market.api.service.password

import cats.effect.IO
import com.pays.market.api.UnitSpec
import com.pays.market.api.model.MarketApiException
import scalikejdbc.NoSession

class UserPasswordAlgebraSpec extends UnitSpec {

  private val userId = 10

  "UserPasswordAlgebra" >> {
    "return an existing user" in {
      for {
        newUser <- UserPasswordRepo.algebra(IO.pure(UserPasswordRepo.userPassword)).findById(userId)(NoSession)
      } yield newUser should beEqualTo(UserPasswordRepo.userPassword)
    }
    "return NotFound exception in case of user does not exist" in {
      val error   = MarketApiException.notFound("not found")
      val input   = IO.raiseError(error)
      val attempt = UserPasswordRepo.algebra(input).findById(userId)(NoSession).attempt.unsafeRunSync()
      attempt must beLeft
      attempt.left.get must beEqualTo(error)
    }
    "return Exception in case of any other error" in {
      val error   = new Exception("random error")
      val input   = IO.raiseError(error)
      val attempt = UserPasswordRepo.algebra(input).findById(userId)(NoSession).attempt.unsafeRunSync()
      attempt must beLeft
      attempt.left.get must beEqualTo(error)
    }
  }
}
