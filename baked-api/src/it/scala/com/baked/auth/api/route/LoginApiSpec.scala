package com.baked.auth.api.route

import cats.effect.IO
import com.baked.auth.api.ItSpec
import com.baked.auth.api.model.BakedAuthError
import com.baked.auth.api.service.login.Login
import com.baked.auth.api.service.login.Login.Token
import org.http4s.Status
import org.specs2.matcher.MatchResult

class LoginApiSpec extends ItSpec {

  private val path = "/api/login"

  private val error = BakedAuthError(
    code = "bad_request",
    message = "user_or_password"
  )

  "LoginApi" >> {
    "return OK for a valid logged user" in {
      val login = Login(
        email = "email@email.com",
        password = "password"
      )
      apis.use { implicit routes =>
        for {
          response <- post"$path".withJsonObj(login).compile
          _ = response.status must beEqualTo(Status.Ok)
          t       <- response.bodyAsJson[Token]
          isValid <- jwtCodec.isValid(t.token).attempt
        } yield isValid must beRight
      }
    }
    "return BAD_REQUEST for no existing email" in {
      val login = Login(
        email = "not-exist@email.com",
        password = "password"
      )
      apis.use { implicit routes =>
        for {
          response <- post"$path".withJsonObj(login).compile
          _ = response.status must beEqualTo(Status.BadRequest)
          responseError <- response.bodyAsJson[BakedAuthError]
        } yield responseError must beEqualTo(error)
      }
    }
    "return BAD_REQUEST for invalid password" in {
      val login = Login(
        email = "email@email.com",
        password = "invalid-password"
      )
      apis.use { implicit routes =>
        for {
          response <- post"$path".withJsonObj(login).compile
          _ = response.status must beEqualTo(Status.BadRequest)
          responseError <- response.bodyAsJson[BakedAuthError]
        } yield responseError must beEqualTo(error)
      }
    }
    "json validation" >> {
      def checkBadRequest(login: Login): IO[MatchResult[Status]] =
        apis.use { implicit routes =>
          for {
            response <- post"$path".withJsonObj(login).compile
          } yield response.status must beEqualTo(Status.BadRequest)
        }
      "return BAD_REQUEST for email longer than 256 characters" in {
        val login = Login(
          email = List.fill(257)("a").mkString,
          password = "password"
        )
        checkBadRequest(login)
      }
      "return BAD_REQUEST for invalid email" in {
        val login = Login(
          email = "e@c",
          password = "password"
        )
        checkBadRequest(login)
      }
      "return BAD_REQUEST for password shorter than 5 characters" in {
        val login = Login(
          email = "email@email.com",
          password = "1234"
        )
        checkBadRequest(login)
      }
      "return BAD_REQUEST for password longer than 64 characters" in {
        val login = Login(
          email = "email@email.com",
          password = List.fill(65)("").mkString
        )
        checkBadRequest(login)
      }
    }
  }
}
