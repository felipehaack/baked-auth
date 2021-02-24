package com.baked.auth.api.service.social.model

import com.wix.accord.dsl._
import io.circe.generic.JsonCodec

object Login {

  @JsonCodec case class WithAccessToken(
    token: String
  )

  object WithAccessToken {
    implicit val Validator = validator[WithAccessToken] { v =>
      v.token is notEmpty
    }
  }

  @JsonCodec case class Me(
    name: String,
    email: String
  )
}
