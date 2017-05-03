package models

import org.joda.time.DateTime
import enumeratum.{Enum, EnumEntry}

case class PushRegistration(
  usercode: String,
  platform: Platform,
  token: String,
  createdAt: DateTime,
  lastFetchedAt: DateTime,
  updatedAt: Option[DateTime],
  deviceString: Option[String]
)

sealed abstract class Platform(val dbValue: String) extends EnumEntry

object Platform extends Enum[Platform] {
  private type V = Platform
  val values = findValues

  case object Apple extends Platform("a")
  case object Google extends Platform("g")
  case object WebPush extends Platform("w")

  def apply(dbValue: String): Platform = unapply(dbValue).getOrElse(throw new IllegalArgumentException(dbValue))

  def unapply(dbValue: String): Option[Platform] = values.find(_.dbValue == dbValue)
}
