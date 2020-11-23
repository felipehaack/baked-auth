package com.pays.market.api.config

object EnvConfig extends Enumeration {
  val Local = Value(0, "local")
  val Dev   = Value(2, "dev")
  val Tst   = Value(3, "tst")
  val Acc   = Value(4, "acc")
  val Prd   = Value(5, "prd")
}
