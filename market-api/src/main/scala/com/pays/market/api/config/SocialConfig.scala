package com.pays.market.api.config

case class SocialGoogleConfig(
  audience: String
)

case class SocialConfig(
  google: SocialGoogleConfig
)
