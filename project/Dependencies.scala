import sbt._

object Dependencies extends AutoPlugin {

  val autoImport = this

  object Cassandra {
    val driver = "com.datastax.oss" % "java-driver-core" % "4.8.0"
  }

  object Cats {
    val version = "2.1.1"
    val core    = "org.typelevel" %% "cats-core"   % version
    val effect  = "org.typelevel" %% "cats-effect" % version
  }

  object Circe {
    private val version = "0.12.3"
    val core            = "io.circe" %% "circe-core"    % version
    val generic         = "io.circe" %% "circe-generic" % version
    val parser          = "io.circe" %% "circe-parser"  % version
  }

  object Http4s {
    private val version = "0.21.7"
    val server          = "org.http4s" %% "http4s-blaze-server" % version
    val dsl             = "org.http4s" %% "http4s-dsl"          % version
  }

  object Specs2 {
    val core = "org.specs2" %% "specs2-core" % "4.10.3"
  }

  object TypeSafe {
    val config = "com.typesafe" % "config" % "1.4.0"
  }

  object Utils {
    val ficus   = "com.iheart"    %% "ficus"           % "1.5.0"
    val logback = "ch.qos.logback" % "logback-classic" % "1.2.3"
  }

}
