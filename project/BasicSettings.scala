import sbt.Keys._
import sbt._

object BasicSettings extends AutoPlugin {

  lazy val Tools         = config("tools") extend Compile
  lazy val toolsSettings = inConfig(Tools)(Defaults.compileSettings)

  override lazy val trigger = allRequirements

  lazy val settings: Seq[Def.Setting[_]] = Seq(
    name := "market-api",
    scalaVersion := "2.12.10",
    organization := "com.market.app",
    scalacOptions ++= Seq(
          "-deprecation",     // Emit warning and location for usages of deprecated APIs.
          "-feature",         // Emit warning and location for usages of features that should be imported explicitly.
          "-unchecked",       // Enable additional warnings where generated code depends on assumptions.
          "-Xfatal-warnings", // Fail the compilation if there are any warnings.
          "-encoding",
          "UTF-8",                         // Adding default encode type
          "-language:implicitConversions", // Otherwise the compiler will ask to import scala.language.implicitConversions
          "-language:higherKinds",         // Enable by default the high kinds for cats
          "-language:postfixOps",          // Enable by default post fix ops for cats
          "-Ypartial-unification"          // This is optional, but it is better to keep as it might result in fail on Http4s
        )
  )

  val itSettings = Defaults.itSettings ++ Seq(
          fork in IntegrationTest := true,
          parallelExecution in IntegrationTest := false,
          testForkedParallel in IntegrationTest := false,
          testOptions in IntegrationTest += Tests.Argument("sequential")
        )

  override def projectSettings: Seq[Setting[_]] =
    super.projectSettings ++ settings ++ toolsSettings ++ itSettings

  override def projectConfigurations =
    super.projectConfigurations :+ Tools :+ IntegrationTest
}
