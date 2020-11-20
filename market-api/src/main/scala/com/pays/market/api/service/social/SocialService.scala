package com.pays.market.api.service.social

import com.pays.market.api.service.social.model.Login

trait SocialService[F[_], T] {
  def getMe(token: T): F[Login.Me]
}
