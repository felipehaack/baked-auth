package com.pays.market.api.config

import scala.concurrent.duration.FiniteDuration

case class CorsConfig(
  urls: List[String],
  maxAge: FiniteDuration
)
