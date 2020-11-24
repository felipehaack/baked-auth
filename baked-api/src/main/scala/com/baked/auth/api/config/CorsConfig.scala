package com.baked.auth.api.config

import scala.concurrent.duration.FiniteDuration

case class CorsConfig(
  urls: List[String],
  maxAge: FiniteDuration
)
