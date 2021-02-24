package com.baked.auth.api.service.social

import com.baked.auth.api.service.social.model.Login
import com.baked.auth.api.util.Log

trait SocialService[F[_]] extends Log {
  def getMe(accessToken: Login.WithAccessToken): F[Login.Me]
}
