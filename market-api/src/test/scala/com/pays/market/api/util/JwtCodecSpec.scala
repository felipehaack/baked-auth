package com.pays.market.api.util

import com.pays.market.api.UnitSpec
import com.pays.market.api.model.MarketApiException

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
        val error = MarketApiException.invalid("token")
        either.left.get must beEqualTo(error)
      }
    }
  }
}
