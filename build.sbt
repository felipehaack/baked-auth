val api = Project("market-api", file("market-api"))
val db  = Project("market-db", file("market-db"))

val root = Project("market", file("."))
  .aggregate(api)
