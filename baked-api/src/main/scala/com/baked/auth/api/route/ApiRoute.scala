package com.baked.auth.api.route

import org.http4s.HttpRoutes

trait ApiRoute[F[_]] {
  def routes: HttpRoutes[F]
}
