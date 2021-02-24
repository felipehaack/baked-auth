package com.baked.auth.api.service.user

import cats.effect.Sync
import com.baked.auth.api.model.BakedAuthException
import scalikejdbc.{ DBSession, WrappedResultSet }

trait UserAlgebra[F[_], T] {
  val rowToObj: WrappedResultSet => T
  def create(user: User.Create)(implicit D: DBSession): F[Long]
  def findById(id: Long)(implicit D: DBSession): F[User]
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

      override def create(
        user: User.Create
      )(
        implicit D: DBSession
      ): F[Long] =
        S.delay {
          D.updateAndReturnSpecifiedGeneratedKey(
            "INSERT INTO users (name, email, status) VALUES (?, ?, ?)",
            user.name,
            user.email,
            user.status
          )("id")
        }

      override def findById(
        id: Long
      )(
        implicit D: DBSession
      ): F[User] = {
        val r = D.first("SELECT * FROM users WHERE id = ?", id)(rowToObj)
        S.fromOption(r, BakedAuthException.notFound("user"))
      }

      override def findByEmail(
        email: String
      )(
        implicit D: DBSession
      ): F[User] = {
        val r = D.first("SELECT * FROM users WHERE email = ?", email)(rowToObj)
        S.fromOption(r, BakedAuthException.notFound("user"))
      }
    }
}
