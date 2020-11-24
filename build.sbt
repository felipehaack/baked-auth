val api = Project("baked-api", file("baked-api"))
val db  = Project("baked-db", file("baked-db"))

val root = Project("baked", file("."))
  .aggregate(api)
