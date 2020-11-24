package com.baked.auth.api.service.password

import cats.effect.Sync
import com.baked.auth.api.db.PostgresDb

trait UserPasswordService[F[_]] {
  def get(userId: Long): F[UserPassword]
}

object UserPasswordService {

  def instance[F[_] : Sync](
    db: PostgresDb[F],
    userPasswordAlgebra: UserPasswordAlgebra[F, UserPassword]
  ): UserPasswordService[F] =
    new UserPasswordService[F] {
      override def get(
        userId: Long
      ): F[UserPassword] =
        db.read { implicit s =>
          userPasswordAlgebra.findById(userId)
        }
    }
}
