package com.baked.auth.api.util

import com.baked.auth.api.UnitSpec
import com.baked.auth.api.model.BakedAuthException

class JwtCodecSpec extends UnitSpec {

  "JwtCodec" >> {
    "create a valid token" in {
      for {
        token  <- jwtCodec.encode(UserRepo.user)
        either <- jwtCodec.isValid(token).attempt
      } yield either must beRight
    }
    "return an error for invalid token" in {
      for {
        either <- jwtCodec.isValid("randomToken").attempt
      } yield {
        either must beLeft
        val error = BakedAuthException.invalid("token")
        either.left.get must beEqualTo(error)
      }
    }
  }
}
