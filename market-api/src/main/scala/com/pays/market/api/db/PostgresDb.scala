package com.pays.market.api.db

import java.sql.Connection

import cats.FlatMap.ops._
import cats.effect.Sync
import cats.syntax.applicativeError._
import com.pays.market.api.db.PostgresSession.{ NoSession, WithSession }
import scalikejdbc.{ ConnectionPool, DB, DBSession, Tx, TxBoundary }

trait PostgresDb[F[_]] {
  def read[A](f: DBSession => F[A]): F[A]
  def transaction[A](f: DBSession => F[A])(implicit S: PostgresSession): F[A]
}

object PostgresDb {

  private def txBoundary[F[_], A](implicit S: Sync[F]): TxBoundary[F[A]] =
    new TxBoundary[F[A]] {
      def finishTx(result: F[A], tx: Tx): F[A] =
        result
          .flatMap { r =>
            S.delay(tx.commit()).map(_ => r)
          }
          .handleErrorWith { e =>
            S.delay(tx.rollbackIfActive()).flatMap(_ => S.raiseError[A](e))
          }

      override def closeConnection(result: F[A], doClose: () => Unit): F[A] =
        result.flatMap { r =>
          S.delay(doClose.apply()).map(_ => r)
        }
    }

  def instance[F[_]](
    connectionPool: ConnectionPool
  )(
    implicit S: Sync[F]
  ): PostgresDb[F] =
    new PostgresDb[F] {
      private def closeConn(conn: Connection): F[Unit] =
        if (conn.isClosed) S.unit
        else S.delay(conn.close())

      override def read[A](f: DBSession => F[A]): F[A] =
        for {
          conn <- S.delay(connectionPool.borrow())
          r    <- DB.apply(conn).readOnly(f)
          _    <- closeConn(conn)
        } yield r

      override def transaction[A](f: DBSession => F[A])(implicit P: PostgresSession): F[A] =
        P match {
          case WithSession(s) => f(s)
          case NoSession =>
            for {
              conn <- S.delay(connectionPool.borrow())
              r    <- DB.apply(conn).localTx(f)(boundary = txBoundary[F, A])
              _    <- closeConn(conn)
            } yield r
        }
    }
}
