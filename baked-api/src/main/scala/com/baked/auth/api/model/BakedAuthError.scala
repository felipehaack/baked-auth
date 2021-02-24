package com.baked.auth.api.model

import io.circe.generic.JsonCodec

@JsonCodec case class BakedAuthInputError(
  value: String,
  message: String
)

@JsonCodec case class BakedAuthError(
  code: String,
  message: String,
  errors: Option[List[BakedAuthInputError]] = None
)
