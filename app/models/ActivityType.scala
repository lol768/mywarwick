package models

sealed abstract class ActivityType(val dbValue: String)

object ActivityType {

  def apply(dbValue: String): ActivityType = dbValue match {
    case ActivityType(t) => t
  }

  def unapply(dbValue: String): Option[ActivityType] = dbValue match {
    case Notification.dbValue => Some(Notification)
    case Activity.dbValue => Some(Activity)
    case _ => None
  }

  case object Notification extends ActivityType("notification")

  case object Activity extends ActivityType("activity")
}

