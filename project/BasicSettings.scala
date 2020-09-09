import sbt.Keys._
import sbt._

object BasicSettings extends AutoPlugin {

  override lazy val trigger = allRequirements

  lazy val settings: Seq[Def.Setting[_]] =
    Seq(
      scalaVersion := "2.12.12",
      organization := "com.pays.market",
      resolvers ++= Seq(
            Resolver.jcenterRepo,
            Resolver.mavenCentral,
            "repo1" at "https://repo1.maven.org/maven2/"
          ),
      scalacOptions ++= Seq(
            "-encoding",
            "UTF-8",                         // source files are in UTF-8
            "-deprecation",                  // warn about use of deprecated APIs
            "-unchecked",                    // warn about unchecked type parameters
            "-feature",                      // warn about misused language features
            "-language:higherKinds",         // allow higher kinded types without `import scala.language.higherKinds`
            "-language:postfixOps",          // Enable by default post fix ops for cats
            "-language:implicitConversions", // Otherwise the compiler will ask to import scala.language.implicitConversions
            "-Xlint",                        // enable handy linter warnings
            "-Xfatal-warnings",              // turn compiler warnings into errors
            "-Ypartial-unification"          // allow the compiler to unify type constructors of different arities
          ),
      javacOptions ++= Seq(
            "-source",
            "1.8",
            "-target",
            "1.8",
            "-Xlint"
          )
    )

  val itSettings = Defaults.itSettings ++ Seq(
          fork in IntegrationTest := true,
          parallelExecution in IntegrationTest := false,
          testForkedParallel in IntegrationTest := false,
          testOptions in IntegrationTest += Tests.Argument("sequential")
        )

  override def projectSettings: Seq[Setting[_]] =
    super.projectSettings ++ settings ++ itSettings

  override def projectConfigurations =
    super.projectConfigurations :+ IntegrationTest
}
