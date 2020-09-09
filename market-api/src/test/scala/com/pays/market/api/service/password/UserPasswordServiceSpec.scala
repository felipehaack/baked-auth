package com.pays.market.api.service.password

import cats.effect.IO
import com.pays.market.api.UnitSpec
import com.pays.market.api.model.MarketApiException
import scalikejdbc.{ DBSession, WrappedResultSet }

class UserPasswordServiceSpec extends UnitSpec {

  private val findByUserId = 10

  private val userPassword = UserPassword(
    encryptedPassword = "password"
  )

  private def mockUserPasswordAlgebra(effect: IO[UserPassword]): UserPasswordAlgebra[IO, UserPassword] =
    new UserPasswordAlgebra[IO, UserPassword] {
      override val rowToObj: WrappedResultSet => UserPassword                      = _ => userPassword
      override def findById(userId: Long)(implicit D: DBSession): IO[UserPassword] = effect
    }

  "UserService" >> {
    "return an existing user" in {
      val userPasswordAlgebra = mockUserPasswordAlgebra(IO.pure(userPassword))
      val userPasswordService = UserPasswordService.instance[IO](
        db = noSessionPostgresDb,
        userPasswordAlgebra = userPasswordAlgebra
      )
      userPasswordService.get(findByUserId).map { u =>
        u must beEqualTo(userPassword)
      }
    }
    "return not found user" in {
      val error               = MarketApiException.notFound("not found user")
      val userPasswordAlgebra = mockUserPasswordAlgebra(IO.raiseError(error))
      val userPasswordService = UserPasswordService.instance[IO](
        db = noSessionPostgresDb,
        userPasswordAlgebra = userPasswordAlgebra
      )
      val either = userPasswordService.get(findByUserId).attempt.unsafeRunSync()
      either must beLeft
      either.left.get must beEqualTo(error)
    }
    "return Exception in case of any error" in {
      val error               = new Exception("random exception")
      val userPasswordAlgebra = mockUserPasswordAlgebra(IO.raiseError(error))
      val userPasswordService = UserPasswordService.instance[IO](
        db = noSessionPostgresDb,
        userPasswordAlgebra = userPasswordAlgebra
      )
      val either = userPasswordService.get(findByUserId).attempt.unsafeRunSync()
      either must beLeft
      either.left.get must beEqualTo(error)
    }
  }
}
