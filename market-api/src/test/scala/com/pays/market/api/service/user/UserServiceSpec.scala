package com.pays.market.api.service.user

import cats.effect.IO
import com.pays.market.api.UnitSpec
import com.pays.market.api.model.MarketApiException
import scalikejdbc.{ DBSession, WrappedResultSet }

class UserServiceSpec extends UnitSpec {

  private val findByEmail = "email@email.com"

  private val user = User(
    id = 1000,
    name = "name",
    email = "email@email.com",
    status = "created"
  )

  private def mockUserAlgebra(effect: IO[User]): UserAlgebra[IO, User] =
    new UserAlgebra[IO, User] {
      override val rowToObj: WrappedResultSet => User                          = _ => user
      override def findByEmail(email: String)(implicit D: DBSession): IO[User] = effect
    }

  "UserService" >> {
    "return an existing user" in {
      val userAlgebra = mockUserAlgebra(IO.pure(user))
      val userService = UserService.instance[IO](
        db = noSessionPostgresDb,
        userAlgebra = userAlgebra
      )
      userService.getByEmail(findByEmail).map { u =>
        u must beEqualTo(user)
      }
    }
    "return not found user" in {
      val error       = MarketApiException.notFound("not found user")
      val userAlgebra = mockUserAlgebra(IO.raiseError(error))
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
      val userAlgebra = mockUserAlgebra(IO.raiseError(error))
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
