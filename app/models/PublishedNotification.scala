package models

import org.joda.time.DateTime
import warwick.sso.Usercode

case class PublishedNotification(
  activityId: String,
  publisherId: String,
  createdAt: DateTime,
  createdBy: Usercode
)

case class PublishedNotificationSave(
  activityId: String,
  publisherId: String,
  createdBy: Usercode
)