package com.baked.auth.api.resouce

import cats.effect.{ Resource, Sync }
import com.zaxxer.hikari.HikariDataSource
import scalikejdbc.{ ConnectionPool, DataSourceConnectionPool }

import scala.util.Try

trait ConnectionPoolResource[F[_]] {
  def resource: Resource[F, ConnectionPool]
}

object ConnectionPoolResource {

  def resource[F[_]](
    hikariDataSource: HikariDataSource
  )(
    implicit S: Sync[F]
  ): Resource[F, ConnectionPool] = instance[F](hikariDataSource).resource

  def instance[F[_]](
    hikariDataSource: HikariDataSource
  )(
    implicit S: Sync[F]
  ): ConnectionPoolResource[F] =
    new ConnectionPoolResource[F] {
      override def resource: Resource[F, ConnectionPool] = {
        val connectionPool                     = new DataSourceConnectionPool(hikariDataSource)
        val make                               = S.delay(connectionPool)
        val release: ConnectionPool => F[Unit] = c => S.delay(Try.apply(c.close))
        Resource.make(make)(release)
      }
    }
}
