package com.pays.market.api.service.user

import cats.effect.Sync
import com.pays.market.api.model.MarketApiException
import scalikejdbc.{ DBSession, WrappedResultSet }

trait UserAlgebra[F[_], T] {
  val rowToObj: WrappedResultSet => T
  def findByEmail(email: String)(implicit D: DBSession): F[User]
}

object UserAlgebra {

  def instance[F[_]](implicit S: Sync[F]): UserAlgebra[F, User] =
    new UserAlgebra[F, User] {
      override val rowToObj: WrappedResultSet => User = row =>
        User(
          id = row.long("id"),
          name = row.string("name"),
          email = row.string("email"),
          status = row.string("status")
        )

      override def findByEmail(
        email: String
      )(
        implicit D: DBSession
      ): F[User] = {
        val r = D.first("select * from users where email = ?", email)(rowToObj)
        S.fromOption(r, MarketApiException.notFound("user"))
      }
    }
}
