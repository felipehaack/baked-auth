package com.pays.market.api

import cats.effect.{ ContextShift, IO, Timer }
import com.pays.market.api.config.JwtConfig
import com.pays.market.api.db.{ PostgresDb, PostgresSession }
import com.pays.market.api.util.JwtCodec
import org.specs2.execute.AsResult
import org.specs2.mutable.Specification
import scalikejdbc.{ DBSession, NoSession }

import scala.concurrent.ExecutionContext

trait UnitMock {
  val jwtCodec = JwtCodec.instance[IO](
    jwtConfig = JwtConfig(
      secret = "secret",
      expireInDays = 10
    )
  )

  val noSessionPostgresDb = new PostgresDb[IO] {
    override def read[A](f: DBSession => IO[A]): IO[A]                                     = f(NoSession)
    override def transaction[A](f: DBSession => IO[A])(implicit S: PostgresSession): IO[A] = f(NoSession)
  }
}

trait UnitSuite {
  implicit val timer: Timer[IO]               = IO.timer(ExecutionContext.global)
  implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  implicit def ioAsResult[A : AsResult]: AsResult[IO[A]] =
    new AsResult[IO[A]] {
      override def asResult(io: => IO[A]) =
        AsResult {
          io.unsafeRunSync()
        }
    }
}

trait UnitSpec extends Specification with UnitSuite with UnitMock with UnitRepo
