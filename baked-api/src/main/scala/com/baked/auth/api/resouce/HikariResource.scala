package com.baked.auth.api.resouce

import cats.effect.{ Resource, Sync }
import com.baked.auth.api.config.DbConfig
import com.zaxxer.hikari.{ HikariConfig, HikariDataSource }

trait HikariResource[F[_]] {
  def resource: Resource[F, HikariDataSource]
}

object HikariResource {

  def resource[F[_] : Sync](
    dbConfig: DbConfig
  ): Resource[F, HikariDataSource] = instance[F](dbConfig).resource

  def instance[F[_]](
    dbConfig: DbConfig
  )(
    implicit S: Sync[F]
  ): HikariResource[F] =
    new HikariResource[F] {
      override def resource: Resource[F, HikariDataSource] = {
        val config = new HikariConfig()
        config.setJdbcUrl(dbConfig.url)
        config.setUsername(dbConfig.user)
        config.setPassword(dbConfig.pwd)
        config.setDriverClassName(dbConfig.driver)

        dbConfig.properties.foreach {
          case (key, value) => config.addDataSourceProperty(key, value)
        }

        val make                                 = S.delay(new HikariDataSource(config))
        val release: HikariDataSource => F[Unit] = c => S.delay(c.close())
        Resource.make(make)(release)
      }
    }
}
