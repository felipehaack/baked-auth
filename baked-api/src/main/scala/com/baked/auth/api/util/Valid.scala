package com.baked.auth.api.util

import com.wix.accord.{ Failure, NullSafeValidator, RuleViolation }

object Valid {
  private val emailRegex =
    "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$".r

  object Email
      extends NullSafeValidator[String](
        v => v.trim.nonEmpty && emailRegex.findFirstMatchIn(v).isDefined,
        v => Failure.apply(Set(RuleViolation.apply(v, "is not email")))
      )
}
