package com.market.app

import cats.effect.{ ConcurrentEffect, Timer }
import org.http4s.server.blaze.BlazeServerBuilder

import scala.concurrent.ExecutionContext

trait ApiApp {

  def application[F[_] : ConcurrentEffect : Timer]: F[Unit] =
    BlazeServerBuilder
      .apply[F](
        executionContext = ExecutionContext.global
      )
      .bindHttp(
        port = 8081,
        host = "0.0.0.0"
      )
      .serve
      .compile
      .drain
}
