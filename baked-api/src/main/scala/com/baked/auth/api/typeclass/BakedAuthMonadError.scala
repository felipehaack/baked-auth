package com.baked.auth.api.typeclass

import cats.FlatMap.ops._
import cats.MonadError
import cats.syntax.ApplicativeErrorSyntax
import com.baked.auth.api.model.BakedAuthException

trait BakedAuthMonadError extends ApplicativeErrorSyntax {

  implicit final class BakedAuthError[F[_], A](effect: F[A]) {
    def logThrow[B <: BakedAuthException](
      log: Throwable => Unit
    )(
      thrown: Throwable => B
    )(
      implicit M: MonadError[F, Throwable]
    ): F[A] =
      effect.handleErrorWith { e =>
        M.catchNonFatal {
            log.apply(e)
          }
          .flatMap { _ =>
            M.raiseError[A](thrown.apply(e))
          }
      }
  }
}
