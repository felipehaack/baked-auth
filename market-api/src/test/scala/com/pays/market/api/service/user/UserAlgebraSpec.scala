package com.pays.market.api.service.user

import cats.effect.IO
import com.pays.market.api.UnitSpec
import com.pays.market.api.model.MarketApiException
import scalikejdbc.{ DBSession, NoSession, WrappedResultSet }

class UserAlgebraSpec extends UnitSpec {

  private val user = User(
    id = 1000,
    name = "name",
    email = "email@email.com",
    status = "created"
  )

  private def mockUserAlgebra(
    effect: IO[User]
  ): UserAlgebra[IO, User] =
    new UserAlgebra[IO, User] {
      override val rowToObj: WrappedResultSet => User = _ => user

      override def findByEmail(email: String)(implicit D: DBSession): IO[User] = effect
    }

  "UserAlgebra" >> {
    "return an existing user" in {
      for {
        newUser <- mockUserAlgebra(IO.pure(user)).findByEmail("find")(NoSession)
      } yield newUser should beEqualTo(user)
    }
    "return NotFound exception in case of user does not exist" in {
      val error   = MarketApiException.notFound("not found")
      val input   = IO.raiseError(error)
      val attempt = mockUserAlgebra(input).findByEmail("find")(NoSession).attempt.unsafeRunSync()
      attempt must beLeft
      attempt.left.get must beEqualTo(error)
    }
    "return Exception in case of any other error" in {
      val error   = new Exception("random error")
      val input   = IO.raiseError(error)
      val attempt = mockUserAlgebra(input).findByEmail("find")(NoSession).attempt.unsafeRunSync()
      attempt must beLeft
      attempt.left.get must beEqualTo(error)
    }
  }
}
