package models

import org.joda.time.DateTime

case class PushRegistration(
  usercode: String,
  platform: Platform,
  token: String,
  createdAt: DateTime
)

sealed abstract class Platform(val dbValue: String)

object Platform {

  val values = Set(Apple, Google)

  case object Apple extends Platform("a")
  case object Google extends Platform("g")

  def apply(dbValue: String): Platform = unapply(dbValue).getOrElse(throw new IllegalArgumentException(dbValue))

  def unapply(dbValue: String): Option[Platform] = values.find(_.dbValue == dbValue)

}
