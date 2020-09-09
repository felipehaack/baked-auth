package com.pays.market.api.config

object EnvConfig extends Enumeration {
  val Dev = Value(0, "dev")
  val Tst = Value(1, "tst")
  val Acc = Value(2, "acc")
  val Prd = Value(3, "prd")
}
