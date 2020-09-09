addCompilerPlugin(Macro.paradise)

libraryDependencies ++= Seq(
  Cats.effect,
  Cats.core,
  Circe.core,
  Circe.generic,
  Circe.parser,
  DB.hikaricp,
  DB.postgres,
  DB.scalike,
  Http4s.dsl,
  Http4s.server,
  Macro.reflect,
  Secure.bcrypt,
  Secure.jwt,
  Specs2.core,
  TypeSafe.config,
  Utils.accord,
  Utils.ficus,
  Utils.logback
)
