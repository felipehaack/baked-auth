package com.baked.auth.api.config

import com.typesafe.config.ConfigFactory
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import net.ceedubs.ficus.readers.EnumerationReader
import net.ceedubs.ficus.readers.ValueReader._

case class AppConfig(
  env: EnvConfig.Value,
  api: ApiConfig,
  db: DbConfig,
  jwt: JwtConfig,
  social: SocialConfig,
  cors: CorsConfig
)

object AppConfig extends EnumerationReader {
  val appConfig: AppConfig = ConfigFactory.load().as[AppConfig]("app")
}
