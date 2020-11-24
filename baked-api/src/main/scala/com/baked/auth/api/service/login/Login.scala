package com.baked.auth.api.service.login

import com.baked.auth.api.util.Valid
import com.wix.accord.dsl._
import io.circe.generic.JsonCodec

@JsonCodec case class  Login(
  email: String,
  password: String
)

object Login {

  implicit val Validator = validator[Login] { v =>
    v.email.length must between(5, 256)
    v.email is Valid.Email

    v.password.length must between(6, 64)
  }

  @JsonCodec case class Token(
    token: String
  )
}
