package com.pays.market.api.model

import scala.util.control.NoStackTrace

abstract class MarketApiException(message: String) extends Exception(message) with NoStackTrace

case class MarketNotFoundException(message: String) extends MarketApiException(message)

case class MarketInvalidException(message: String) extends MarketApiException(message)

case class MarketInternalServerException(message: String) extends MarketApiException(message)

case class MarketInvalidJsonException(errors: List[MarketApiInputError], message: String)
    extends MarketApiException(message)

object MarketApiException {
  def notFound(message: String): MarketNotFoundException =
    MarketNotFoundException(message)

  def invalid(message: String): MarketInvalidException =
    MarketInvalidException(message)

  def invalidInputs(errors: List[MarketApiInputError], message: String): MarketInvalidJsonException =
    MarketInvalidJsonException(errors, message)

  def internalError(message: String): MarketInternalServerException =
    MarketInternalServerException(message)
}
