package models.news

import org.joda.time.{DateTime, LocalDateTime}
import system.TimeZones
import warwick.sso.Usercode

case class NotificationData(
  text: String,
  providerId: String,
  linkHref: Option[String],
  publishDateSet: Boolean,
  publishDate: LocalDateTime
) {
  def toSave(usercode: Usercode, publisherId: String) = NotificationSave(
    text = text,
    linkHref = linkHref,
    publisherId = publisherId,
    usercode = usercode,
    providerId = providerId,
    publishDate = (if (publishDateSet) publishDate else LocalDateTime.now).toDateTime(TimeZones.LONDON)
  )
}

case class NotificationSave(
  text: String,
  linkHref: Option[String],
  publisherId: String,
  usercode: Usercode,
  providerId: String,
  publishDate: DateTime
)

