package com.baked.auth.api.model

import io.circe.generic.JsonCodec

@JsonCodec case class MarketApiInputError(
  value: String,
  message: String
)

@JsonCodec case class MarketApiError(
  code: String,
  message: String,
  errors: Option[List[MarketApiInputError]] = None
)
