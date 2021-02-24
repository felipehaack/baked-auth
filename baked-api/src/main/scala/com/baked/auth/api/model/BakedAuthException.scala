package com.baked.auth.api.model

import scala.util.control.NoStackTrace

abstract class BakedAuthException(message: String) extends Exception(message) with NoStackTrace

case class BakedAuthNotFoundException(message: String) extends BakedAuthException(message)

case class BakedAuthInvalidException(message: String) extends BakedAuthException(message)

case class BakedAuthInternalServerException(message: String) extends BakedAuthException(message)

case class BakedAuthInvalidJsonException(errors: List[BakedAuthInputError], message: String)
    extends BakedAuthException(message)

object BakedAuthException {
  def notFound(message: String): BakedAuthNotFoundException =
    BakedAuthNotFoundException(message)

  def invalid(message: String): BakedAuthInvalidException =
    BakedAuthInvalidException(message)

  def invalidInputs(errors: List[BakedAuthInputError], message: String): BakedAuthInvalidJsonException =
    BakedAuthInvalidJsonException(errors, message)

  def internalError(message: String): BakedAuthInternalServerException =
    BakedAuthInternalServerException(message)
}
