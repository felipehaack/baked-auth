package com.pays.market.db

import com.typesafe.config.ConfigFactory
import liquibase.integration.commandline.Main

object MainApp extends App {
  val config = ConfigFactory.load()

  val changelog = "db/changelog.xml"
  val url       = config.getString("db.url")
  val user      = config.getString("db.username")
  val pwd       = config.getString("db.password")

  val newArgs = List(
      s"--changeLogFile=$changelog",
      s"--url=$url",
      s"--username=$user",
      s"--password=$pwd"
    ) ++ args

  Main.main(newArgs.toArray)
}
