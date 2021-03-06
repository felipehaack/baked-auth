package com.baked.auth.api.route

import cats.effect.IO
import com.baked.auth.api.ItSpec
import com.baked.auth.api.service.login.Login.Token
import com.baked.auth.api.service.social.model.Login.WithAccessToken
import org.http4s.Status
import org.specs2.matcher.MatchResult

class LoginGoogleApiSpec extends ItSpec {

  private val path = "/api/login/google"

  "LoginGoogleApi" >> {
    def assertOK(login: WithAccessToken): IO[MatchResult[Either[Throwable, Unit]]] =
      apis.use { implicit routes =>
        for {
          response <- post"$path".withJsonObj(login).compile
          _ = response.status must beEqualTo(Status.Ok)
          t       <- response.bodyAsJson[Token]
          isValid <- jwtCodec.isValid(t.token).attempt
        } yield isValid must beRight
      }
    "return OK for an valid and existing user" in {
      val login = WithAccessToken.apply(
        token = "email@email.com"
      )
      assertOK(login)
    }
    "return OK for a new created user" in {
      val loginNewUser = WithAccessToken.apply(
        token = "newuser@email.com"
      )
      assertOK(loginNewUser)
    }
    "json validation" >> {
      def checkBadRequest(login: WithAccessToken): IO[MatchResult[Status]] =
        apis.use { implicit routes =>
          for {
            response <- post"$path".withJsonObj(login).compile
          } yield response.status must beEqualTo(Status.BadRequest)
        }
      "return BAD_REQUEST for an empty token" in {
        val emptyLogin = WithAccessToken.apply("")
        checkBadRequest(emptyLogin)
      }
    }
  }
}
