package com.baked.auth.api.config

case class SocialAudienceConfig(
  audience: String
)

case class SocialConfig(
  google: SocialAudienceConfig,
  facebook: SocialAudienceConfig
)
