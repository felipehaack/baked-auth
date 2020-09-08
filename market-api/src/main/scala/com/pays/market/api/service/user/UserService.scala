package com.pays.market.api.service.user

import cats.effect.Sync
import com.pays.market.api.db.PostgresDb

trait UserService[F[_]] {
  def getByEmail(email: String): F[User]
}

object UserService {

  def instance[F[_]](
    db: PostgresDb[F],
    userAlgebra: UserAlgebra[F, User]
  )(
    implicit S: Sync[F]
  ): UserService[F] =
    new UserService[F] {
      override def getByEmail(
        email: String
      ): F[User] =
        db.read { implicit session =>
          userAlgebra.findByEmail(email)
        }
    }
}
