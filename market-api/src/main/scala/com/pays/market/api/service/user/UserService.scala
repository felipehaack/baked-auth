package com.pays.market.api.service.user

import com.pays.market.api.db.PostgresDb
import com.pays.market.api.db.PostgresSession.NoSession

trait UserService[F[_]] {
  def create(user: User.Create): F[Long]
  def getById(id: Long): F[User]
  def getByEmail(email: String): F[User]
}

object UserService {

  def instance[F[_]](
    db: PostgresDb[F],
    userAlgebra: UserAlgebra[F, User]
  ): UserService[F] =
    new UserService[F] {
      override def create(
        user: User.Create
      ): F[Long] =
        db.transaction { implicit session =>
          userAlgebra.create(user)
        }(NoSession)

      override def getById(
        id: Long
      ): F[User] =
        db.read { implicit session =>
          userAlgebra.findById(id)
        }

      override def getByEmail(
        email: String
      ): F[User] =
        db.read { implicit session =>
          userAlgebra.findByEmail(email)
        }
    }
}
