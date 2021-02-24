package com.baked.auth.api.service.password

import cats.effect.Sync
import com.baked.auth.api.model.BakedAuthException
import scalikejdbc.{ DBSession, WrappedResultSet }

trait UserPasswordAlgebra[F[_], T] {
  val rowToObj: WrappedResultSet => T
  def findById(userId: Long)(implicit D: DBSession): F[UserPassword]
}

object UserPasswordAlgebra {

  def instance[F[_]](implicit S: Sync[F]): UserPasswordAlgebra[F, UserPassword] =
    new UserPasswordAlgebra[F, UserPassword] {
      override val rowToObj: WrappedResultSet => UserPassword = row =>
        UserPassword(
          encryptedPassword = row.string("encrypted_password")
        )

      override def findById(
        userId: Long
      )(
        implicit D: DBSession
      ): F[UserPassword] = {
        val r = D.first("SELECT * FROM user_passwords WHERE user_id = ?", userId)(rowToObj)
        S.fromOption(r, BakedAuthException.notFound("user_password"))
      }
    }
}
