package com.baked.auth.api.util

import org.slf4j.LoggerFactory

trait Log {
  val logger = LoggerFactory.getLogger(getClass)
}
