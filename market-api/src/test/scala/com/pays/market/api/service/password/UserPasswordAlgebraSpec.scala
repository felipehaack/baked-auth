package com.pays.market.api.service.password

import cats.effect.IO
import com.pays.market.api.UnitSpec
import com.pays.market.api.model.MarketApiException
import scalikejdbc.{ DBSession, NoSession, WrappedResultSet }

class UserPasswordAlgebraSpec extends UnitSpec {

  private val userId = 10

  private val userPassword = UserPassword(
    encryptedPassword = "random_password"
  )

  private def mockUserPasswordAlgebra(
    effect: IO[UserPassword]
  ): UserPasswordAlgebra[IO, UserPassword] =
    new UserPasswordAlgebra[IO, UserPassword] {
      override val rowToObj: WrappedResultSet => UserPassword = _ => userPassword

      override def findById(userId: Long)(implicit D: DBSession): IO[UserPassword] = effect
    }

  "UserPasswordAlgebra" >> {
    "return an existing user" in {
      for {
        newUser <- mockUserPasswordAlgebra(IO.pure(userPassword)).findById(userId)(NoSession)
      } yield newUser should beEqualTo(userPassword)
    }
    "return NotFound exception in case of user does not exist" in {
      val error   = MarketApiException.notFound("not found")
      val input   = IO.raiseError(error)
      val attempt = mockUserPasswordAlgebra(input).findById(userId)(NoSession).attempt.unsafeRunSync()
      attempt must beLeft
      attempt.left.get must beEqualTo(error)
    }
    "return Exception in case of any other error" in {
      val error   = new Exception("random error")
      val input   = IO.raiseError(error)
      val attempt = mockUserPasswordAlgebra(input).findById(userId)(NoSession).attempt.unsafeRunSync()
      attempt must beLeft
      attempt.left.get must beEqualTo(error)
    }
  }
}
