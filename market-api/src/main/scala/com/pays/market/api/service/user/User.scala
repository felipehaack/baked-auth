package com.pays.market.api.service.user

case class User(
  id: Long,
  name: String,
  email: String,
  status: String
)

object User {

  case class Create(
    name: String,
    email: String,
    status: String = "active"
  )
}
