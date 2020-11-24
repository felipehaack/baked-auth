package com.baked.auth.api.service.social

import com.baked.auth.api.service.social.model.Login

trait SocialService[F[_], T] {
  def getMe(token: T): F[Login.Me]
}
