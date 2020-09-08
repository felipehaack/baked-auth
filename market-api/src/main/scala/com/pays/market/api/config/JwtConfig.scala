package com.pays.market.api.config

case class JwtConfig(
  secret: String,
  expireInDays: Int
) {
  def expireInMilliseconds: Long = System.currentTimeMillis() + (1000 * 60 * 60 * 24 * expireInDays)
}
