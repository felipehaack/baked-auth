package com.pays.market.api

import cats.SemigroupK.ops._
import cats.effect.{ ConcurrentEffect, ContextShift, Timer }
import com.pays.market.api.config.AppConfig.appConfig
import com.pays.market.api.db.PostgresDb
import com.pays.market.api.injection.Injector
import com.pays.market.api.resouce.{ ConnectionPoolResource, HikariResource }
import com.pays.market.api.util.JwtCodec
import org.http4s.HttpRoutes
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.CORS
import org.http4s.syntax.KleisliSyntax

import scala.concurrent.ExecutionContext

trait ApiApp extends KleisliSyntax {

  def application[F[_] : ConcurrentEffect : Timer : ContextShift]: F[Unit] = {
    val resource = for {
      hikariDataSource <- HikariResource.resource[F](
        dbConfig = appConfig.db
      )
      connection <- ConnectionPoolResource.resource[F](
        hikariDataSource = hikariDataSource
      )
    } yield connection

    resource.use { connectionPool =>
      val db = PostgresDb.instance[F](
        connectionPool = connectionPool
      )

      val jwtCodec = JwtCodec.instance[F](
        jwtConfig = appConfig.jwt
      )

      val injection = Injector.singleton[F](
        appConfig = appConfig,
        db = db,
        jwtCodec = jwtCodec
      )

      val httpRoutes = injection.apis
        .map(_.routes)
        .foldLeft(HttpRoutes.empty)(_ <+> _)

      val corsConfig = injection.cors

      val routeWithCors = CORS.apply(httpRoutes, corsConfig)

      BlazeServerBuilder
        .apply[F](
          executionContext = ExecutionContext.global
        )
        .withHttpApp(
          httpApp = routeWithCors.orNotFound
        )
        .bindHttp(
          port = appConfig.api.port,
          host = appConfig.api.host
        )
        .serve
        .compile
        .drain
    }
  }
}
