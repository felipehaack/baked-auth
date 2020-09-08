package com.pays.market.api.service.password

import cats.effect.Sync
import com.pays.market.api.model.MarketApiException
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
        val r = D.first("select * from user_passwords where user_id = ?", userId)(rowToObj)
        S.fromOption(r, MarketApiException.notFound("user_password"))
      }
    }
}
