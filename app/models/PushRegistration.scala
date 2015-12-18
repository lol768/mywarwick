package models

import org.joda.time.DateTime

case class PushRegistration(
  usercode: String,
  platform: String,
  token: String,
  createdAt: DateTime
)
