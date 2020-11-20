package com.pays.market.api.util

import com.pays.market.api.config.CorsConfig
import org.http4s.server.middleware.CORSConfig

object Cors {

  def cors(config: CorsConfig): CORSConfig =
    CORSConfig(
      anyOrigin = false,
      anyMethod = true,
      allowCredentials = true,
      allowedOrigins = config.urls.contains,
      allowedMethods = Some(Set("GET", "POST", "PUT", "DELETE")),
      maxAge = config.maxAge.toSeconds
    )
}
