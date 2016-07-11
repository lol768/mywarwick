package models.news

import warwick.sso.Usercode

case class NotificationData(
  text: String,
  linkHref: Option[String]
) {
  def toSave(usercode: Usercode, providerId: String) = NotificationSave(
    text = text,
    linkHref = linkHref,
    publisherId = providerId,
    usercode = usercode
  )
}

case class NotificationSave(
  text: String,
  linkHref: Option[String],
  publisherId: String,
  usercode: Usercode
)

