package com.pays.market.api.route

import cats.data.EitherT
import cats.effect.Sync
import cats.syntax.AllSyntax
import com.pays.market.api.model._
import com.wix.accord.{ Failure, Success, Validator }
import fs2.Chunk
import io.circe.parser.parse
import io.circe.{ Decoder, Encoder, Error, Json, Printer }
import org.http4s.EntityEncoder.simple
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.`Content-Type`

trait Api[F[_]] extends Http4sDsl[F] with AllSyntax {

  private val printer            = Printer.noSpaces.copy(dropNullValues = true)
  private val defaultContentType = `Content-Type`(MediaType.application.json).withCharset(DefaultCharset)

  implicit def entityDecodeString(implicit S: Sync[F]): EntityDecoder[F, String] =
    org.http4s.EntityDecoder.text[F](S, DefaultCharset)

  implicit def entityEncoderFromEncoder[A](implicit E: Encoder[A]): EntityEncoder[F, A] =
    simple(defaultContentType) { obj =>
      val output = printer.print(E.apply(obj))
      Chunk.bytes(output.getBytes(DefaultCharset.nioCharset))
    }

  implicit class EffectOps[T](f: F[T]) {
    def asJson(
      implicit S: Sync[F],
      E: Encoder[T]
    ): F[Response[F]] =
      f.flatMap {
          case false     => NotFound.apply()
          case () | true => Ok.apply()
          case v         => Ok.apply(v)
        }
        .handleErrorWith {
          case e: MarketNotFoundException =>
            NotFound.apply(MarketApiError("not_found", e.message))
          case e: MarketInvalidException =>
            BadRequest.apply(MarketApiError("bad_request", e.message))
          case e: MarketInvalidJsonException =>
            BadRequest.apply(MarketApiError("bad_request", e.message, Some(e.errors)))
          case e: MarketInternalServerException =>
            InternalServerError.apply(MarketApiError("internal_error", e.message))
          case e =>
            InternalServerError.apply(MarketApiError("internal_error", e.getMessage))
        }
  }

  implicit class RequestOps(r: Request[F]) {
    def decodeJson[T : Decoder](
      implicit S: Sync[F],
      V: Validator[T]
    ): F[T] =
      r.bodyText
        .compile
        .toList
        .map(_.headOption)
        .flatMap {
          case Some(bodyStr) =>
            val eitherT = for {
              parsedBody <- EitherT.apply[F, Error, Json](S.pure(parse(bodyStr)))
              obj        <- EitherT.apply[F, Error, T](S.pure(parsedBody.as[T]))
            } yield obj

            eitherT.foldF(error => S.raiseError[T](MarketApiException.invalid(error.getMessage)), S.pure)
          case None => S.raiseError[T](MarketApiException.invalid("malformed_body"))
        }
        .flatMap { v =>
          V.apply(v) match {
            case Success => S.pure(v)
            case Failure(violations) =>
              val errors = violations.toList.map { violation =>
                MarketApiInputError(violation.value.toString, violation.constraint)
              }
              S.raiseError[T](MarketApiException.invalidInputs(errors, "invalid_inputs"))
          }
        }
  }
}
