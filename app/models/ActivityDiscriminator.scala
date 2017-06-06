package models

sealed abstract class ActivityDiscriminator(val dbValue: String)

object ActivityDiscriminator {

  val values = Set(Notification, Activity)

  case object Notification extends ActivityDiscriminator("notification")
  case object Activity extends ActivityDiscriminator("activity")

  def apply(dbValue: String): ActivityDiscriminator = unapply(dbValue).getOrElse(throw new IllegalArgumentException(dbValue))

  def unapply(dbValue: String): Option[ActivityDiscriminator] = values.find(_.dbValue == dbValue)

}

