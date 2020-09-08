package com.pays.market.api.service.login

import com.pays.market.api.util.Validators
import com.wix.accord.dsl._
import io.circe.generic.JsonCodec

@JsonCodec case class Login(
  email: String,
  password: String
)

object Login {

  implicit val Validator = validator[Login] { v =>
    v.email.length must between(5, 256)
    v.email is Validators.Email

    v.password.length must between(6, 64)
  }

  @JsonCodec case class Token(
    token: String
  )
}
