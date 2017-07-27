package models.news

import models.ActivitySave
import org.joda.time.LocalDateTime
import system.TimeZones
import warwick.sso.Usercode

object NotificationData {
  val publishNotificationType = "mywarwick-user-publish-notification"
}

case class NotificationData(
  text: String,
  providerId: String,
  linkHref: Option[String],
  publishDateSet: Boolean,
  publishDate: LocalDateTime
) {
  def toSave(usercode: Usercode, publisherId: String) = ActivitySave(
    usercode,
    publisherId,
    providerId = providerId,
    shouldNotify = true,
    `type` = NotificationData.publishNotificationType,
    title = text,
    url = linkHref,
    publishedAt = if (publishDateSet) Some(publishDate.toDateTime(TimeZones.LONDON)) else None
  )
}

