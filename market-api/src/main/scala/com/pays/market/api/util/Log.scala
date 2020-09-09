package com.pays.market.api.util

import org.slf4j.LoggerFactory

trait Log {
  val logger = LoggerFactory.getLogger(getClass)
}
