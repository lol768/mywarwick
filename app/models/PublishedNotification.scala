package models

import org.joda.time.DateTime
import warwick.sso.Usercode

case class PublishedNotification(
  activityId: String,
  publisherId: String,
  createdAt: DateTime,
  createdBy: Usercode,
  updatedAt: Option[DateTime],
  updatedBy: Option[Usercode]
)

case class PublishedNotificationSave(
  activityId: String,
  publisherId: String,
  changedBy: Usercode
)