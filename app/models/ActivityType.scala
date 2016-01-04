package models

sealed abstract class ActivityType(val dbValue: String)

object ActivityType {

  val values = Set(Notification, Activity)

  case object Notification extends ActivityType("notification")
  case object Activity extends ActivityType("activity")

  def apply(dbValue: String): ActivityType = unapply(dbValue).getOrElse(throw new IllegalArgumentException(dbValue))

  def unapply(dbValue: String): Option[ActivityType] = values.find(_.dbValue == dbValue)

}

