package com.pays.market.api.db

import scalikejdbc.DBSession

trait PostgresSession {
  val session: Option[DBSession]
}

object PostgresSession {
  implicit def toSession(s: DBSession): PostgresSession = WithSession(s)

  object NoSession extends PostgresSession {
    override val session: Option[DBSession] = None
  }

  case class WithSession(s: DBSession) extends PostgresSession {
    override val session: Option[DBSession] = Some(s)
  }
}
