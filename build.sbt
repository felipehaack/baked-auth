libraryDependencies ++= Seq(
  Cassandra.driver,
  Cats.core,
  Cats.effect,
  Circe.core,
  Circe.generic,
  Circe.parser,
  Http4s.server,
  Http4s.dsl,
  Specs2.core,
  TypeSafe.config,
  Utils.ficus,
  Utils.logback
)
