package com.pays.market.api

import java.io.InputStream

import cats.SemigroupK.ops._
import cats.data.EitherT
import cats.effect.{ IO, Sync }
import cats.kernel.instances.StringInstances
import cats.{ Monad, MonadError }
import com.pays.market.api.config.AppConfig
import com.pays.market.api.config.AppConfig.appConfig
import com.pays.market.api.db.PostgresDb
import com.pays.market.api.injection.Injector
import com.pays.market.api.resouce.{ ConnectionPoolResource, HikariResource }
import com.pays.market.api.util.{ FileReader, JwtCodec }
import fs2.Stream
import io.circe.{ parser, Decoder, Encoder }
import org.http4s._
import org.http4s.syntax.KleisliSyntax
import org.specs2.execute.AsResult
import org.specs2.mutable.{ Before, Specification }

import scala.concurrent.ExecutionContext
import scala.io.Source

trait ItSpec extends Specification with ItHttpHelper with ItSuite with ItJsonHelper with ItDbSample with Before {

  override def before: Any = {
    val r = for {
      _ <- truncateTables
      _ <- insertionSample
    } yield ()
    r.unsafeRunSync()
  }
}

trait ItHttpHelper extends KleisliSyntax with StringInstances {

  implicit final class RichRequestString(sc: StringContext) {
    def get(args: Any*): Request[IO]    = asRequest(Method.GET, args: _*)
    def post(args: Any*): Request[IO]   = asRequest(Method.POST, args: _*)
    def put(args: Any*): Request[IO]    = asRequest(Method.PUT, args: _*)
    def delete(args: Any*): Request[IO] = asRequest(Method.DELETE, args: _*)

    private def asRequest(method: Method, args: Any*): Request[IO] = {
      val strings     = sc.parts.iterator
      val expressions = args.iterator
      val buf         = new StringBuffer(strings.next)
      while (strings.hasNext) {
        buf.append(expressions.next())
        buf.append(strings.next())
      }
      val uri = Uri.apply(path = buf.toString)
      Request[IO]()
        .withUri(uri)
        .withMethod(method)
        .withHeaders(Headers.of(Header.apply("content-type", "application/json")))
    }
  }

  implicit final class RichResponse(r: Response[IO]) {
    def bodyAsString(implicit S: Sync[IO]): IO[String] =
      r.bodyText.compile.foldMonoid

    def bodyAsJson[T](implicit S: Sync[IO], J: JsonCodec[IO, T]): IO[T] =
      bodyAsString.flatMap(J.decode)
  }

  implicit final class RichRequest(r: Request[IO]) {
    def withJsonRaw(rawJson: String): Request[IO] =
      r.withBodyStream(Stream.emits(rawJson.getBytes()))

    def withJsonObj[T](obj: T)(implicit J: JsonCodec[IO, T]): Request[IO] =
      withJsonRaw(J.encode(obj))

    def withCustomHeaders(headers: Map[String, String]): Request[IO] = {
      val lHeaders = headers.map { case (k, v) => Header.apply(k, v) }.toList
      r.withHeaders(lHeaders: _*)
    }

    def compile(implicit routes: HttpRoutes[IO]): IO[Response[IO]] = routes.orNotFound(r)
  }
}

trait ItSuite {
  implicit def ioAsResult[A : AsResult]: AsResult[IO[A]] =
    new AsResult[IO[A]] {
      override def asResult(io: => IO[A]) =
        AsResult {
          io.unsafeRunSync()
        }
    }
}

trait JsonCodec[F[_], T] {
  def decode(json: String): F[T]
  def encode(obj: T): String
}

trait ItJsonHelper {

  private def decodeJson[F[_] : Monad, T : Decoder](json: String): EitherT[F, String, T] =
    parser.parse(json) match {
      case Left(parsingFailure) => EitherT.leftT(parsingFailure.message)
      case Right(json) =>
        json.as[T] match {
          case Left(decodingFailure) => EitherT.leftT(decodingFailure.message)
          case Right(value)          => EitherT.rightT(value)
        }
    }

  implicit def jsonCodec[T](
    implicit E: Encoder[T],
    D: Decoder[T],
    M: MonadError[IO, Throwable]
  ): JsonCodec[IO, T] =
    new JsonCodec[IO, T] {
      override def decode(json: String): IO[T] =
        decodeJson[IO, T](json).value.flatMap {
          case Left(error)  => M.raiseError(new Exception(error))
          case Right(value) => IO.apply(value)
        }

      override def encode(obj: T): String = E.apply(obj).noSpaces
    }
}

trait Instances {

  implicit val contextShift = IO.contextShift(ExecutionContext.global)

  val jwtCodec = JwtCodec.instance[IO](
    jwtConfig = AppConfig.appConfig.jwt
  )

  val resources = for {
    hikariDataSource <- HikariResource.resource[IO](
      dbConfig = appConfig.db
    )
    connection <- ConnectionPoolResource.resource[IO](
      hikariDataSource = hikariDataSource
    )
  } yield connection

  val apis = resources.map { connectionPool =>
    val db = PostgresDb.instance[IO](
      connectionPool = connectionPool
    )
    val singleton = Injector.singleton[IO](
      appConfig = appConfig,
      db = db,
      jwtCodec = jwtCodec
    )
    singleton.apis.map(_.routes).foldLeft(HttpRoutes.empty[IO])(_ <+> _)
  }
}

trait ItDbSample extends Instances {

  private def runSql(file: String): IO[Unit] = {
    val r = for {
      connectionPool <- resources
      inputStream    <- FileReader.read[IO, InputStream](file)
    } yield (connectionPool, inputStream)
    r.use {
      case (connectionPool, inputStream) =>
        IO.apply {
          val fileStr    = Source.fromInputStream(inputStream).mkString
          val connection = connectionPool.borrow()
          val nativeSql  = connection.nativeSQL(fileStr)
          connection.prepareStatement(nativeSql).execute()
        }
    }
  }

  val insertionSample: IO[Unit] = runSql("/db/seed.sql")
  val truncateTables: IO[Unit]  = runSql("/db/truncate.sql")
}
