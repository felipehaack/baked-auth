package com.baked.auth.api.util

import java.io.{ FileInputStream, InputStream }

import cats.effect.{ Resource, Sync }

import scala.io.{ BufferedSource, Source }

sealed trait FileReader[T] {
  def read[F[_]](file: String)(implicit S: Sync[F]): Resource[F, T]
}

object FileReader {

  implicit object FromAbsoluteInputStream extends FileReader[FileInputStream] {
    override def read[F[_]](file: String)(implicit S: Sync[F]): Resource[F, FileInputStream] =
      Resource.fromAutoCloseable(S.delay(new FileInputStream(file)))
  }

  implicit object FromResourceBufferedSource extends FileReader[BufferedSource] {
    override def read[F[_]](file: String)(implicit S: Sync[F]): Resource[F, BufferedSource] =
      Resource.fromAutoCloseable(S.delay(Source.fromResource(file)))
  }

  implicit object FromResourceFileInputStream extends FileReader[InputStream] {
    override def read[F[_]](file: String)(implicit S: Sync[F]): Resource[F, InputStream] =
      Resource.fromAutoCloseable(S.delay(getClass.getResourceAsStream(file)))
  }

  def apply[T : FileReader]: FileReader[T] = implicitly

  def read[F[_] : Sync, T : FileReader](file: String): Resource[F, T] = apply[T].read[F](file)
}
