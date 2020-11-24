package com.baked.auth.api.config

case class SocialGoogleConfig(
  audience: String
)

case class SocialConfig(
  google: SocialGoogleConfig
)
