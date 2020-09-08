package com.pays.market.api.config

case class DbConfig(
  url: String,
  user: String,
  pwd: String,
  driver: String,
  properties: Map[String, String]
)
