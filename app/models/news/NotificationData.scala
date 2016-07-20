package models.news

import warwick.sso.Usercode

case class NotificationData(
  text: String,
  providerId: String,
  linkHref: Option[String]
) {
  def toSave(usercode: Usercode, publisherId: String) = NotificationSave(
    text = text,
    linkHref = linkHref,
    publisherId = publisherId,
    usercode = usercode,
    providerId = providerId
  )
}

case class NotificationSave(
  text: String,
  linkHref: Option[String],
  publisherId: String,
  usercode: Usercode,
  providerId: String
)

