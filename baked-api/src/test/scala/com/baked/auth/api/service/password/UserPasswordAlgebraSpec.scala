package com.baked.auth.api.service.password

import cats.effect.IO
import com.baked.auth.api.UnitSpec
import com.baked.auth.api.model.BakedAuthException
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
      val error   = BakedAuthException.notFound("not found")
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
