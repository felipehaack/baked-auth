import sbt._

object Dependencies extends AutoPlugin {

  val autoImport = this

  object Cats {
    val version = "2.1.1"
    val effect  = "org.typelevel" %% "cats-effect" % version
    val core    = "org.typelevel" %% "cats-core"   % version
  }

  object Circe {
    private val version = "0.12.3"
    val core            = "io.circe" %% "circe-core"    % version
    val generic         = "io.circe" %% "circe-generic" % version
    val parser          = "io.circe" %% "circe-parser"  % version
  }

  object DB {
    val hikaricp  = "com.zaxxer"       % "HikariCP"       % "3.4.5"
    val liquibase = "org.liquibase"    % "liquibase-core" % "4.0.0"
    val postgres  = "org.postgresql"   % "postgresql"     % "42.2.16"
    val scalike   = "org.scalikejdbc" %% "scalikejdbc"    % "3.5.0"
  }

  object Http4s {
    private val version = "0.21.7"
    val dsl             = "org.http4s" %% "http4s-dsl"          % version
    val server          = "org.http4s" %% "http4s-blaze-server" % version
  }

  object Macro {
    private val paradiseVersion = "2.1.1"
    private val reflectVersion  = "2.12.9"
    val paradise                = "org.scalamacros" % "paradise"      % paradiseVersion cross CrossVersion.full
    val reflect                 = "org.scala-lang"  % "scala-reflect" % reflectVersion
  }

  object Secure {
    val bcrypt = "com.github.t3hnar" %% "scala-bcrypt" % "4.1"
    val jwt    = "com.auth0"          % "java-jwt"     % "3.10.3"
  }

  object Specs2 {
    val core = "org.specs2" %% "specs2-core" % "4.10.3"
  }

  object TypeSafe {
    val config = "com.typesafe" % "config" % "1.4.0"
  }

  object Utils {
    val accord  = "com.wix"       %% "accord-core"     % "0.7.6"
    val ficus   = "com.iheart"    %% "ficus"           % "1.5.0"
    val logback = "ch.qos.logback" % "logback-classic" % "1.2.3"
  }
}
