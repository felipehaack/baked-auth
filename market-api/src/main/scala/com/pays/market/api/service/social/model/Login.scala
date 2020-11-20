package com.pays.market.api.service.social.model

import com.wix.accord.dsl._
import io.circe.generic.JsonCodec

object Login {

  @JsonCodec case class WithGoogle(
    token: String
  )

  object WithGoogle {
    implicit val Validator = validator[WithGoogle] { v =>
      v.token is notEmpty
    }
  }

  @JsonCodec case class Me(
    name: String,
    email: String
  )
}
