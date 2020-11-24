package com.baked.auth.api

import cats.effect.IO
import com.baked.auth.api.service.password.{ UserPassword, UserPasswordAlgebra }
import com.baked.auth.api.service.user.{ User, UserAlgebra }
import scalikejdbc.{ DBSession, WrappedResultSet }

trait UnitRepo {

  object UserRepo {
    val user = User(
      id = 1000,
      name = "name",
      email = "email@email.com",
      status = "created"
    )

    val pureUser = IO.pure(user)

    def algebra(
      byEmail: IO[User],
      byId: IO[User]
    ): UserAlgebra[IO, User] =
      new UserAlgebra[IO, User] {
        override val rowToObj: WrappedResultSet => User                          = _ => user
        override def findByEmail(email: String)(implicit D: DBSession): IO[User] = byEmail
        override def findById(id: Long)(implicit D: DBSession): IO[User]         = byId
        override def create(user: User.Create)(implicit D: DBSession): IO[Long]  = IO.apply(1000L)
      }
  }

  object UserPasswordRepo {
    val userPassword = UserPassword(
      encryptedPassword = "$2a$10$stCOedfG640FLsJ2UmdPo.1P/yy.1ybx4stkNZowefdmBw8wCIQMy"
    )

    def algebra(effect: IO[UserPassword]): UserPasswordAlgebra[IO, UserPassword] =
      new UserPasswordAlgebra[IO, UserPassword] {
        override val rowToObj: WrappedResultSet => UserPassword                      = _ => userPassword
        override def findById(userId: Long)(implicit D: DBSession): IO[UserPassword] = effect
      }
  }

}
