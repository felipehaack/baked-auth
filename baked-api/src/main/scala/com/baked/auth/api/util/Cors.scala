package com.baked.auth.api.util

import com.baked.auth.api.config.CorsConfig
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
