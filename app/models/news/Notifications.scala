package models.news

import models.{ActivityPrototype, ActivityRecipients}

case class PublishNotification(item: NotificationSave, recipients: String) {
  import PublishNotification._

  lazy val recipientUsercodes = recipients.split(',').map(_.trim)

  def activityPrototype = {
    ActivityPrototype(
      providerId = NEWS_PROVIDER_ID,
      `type` = "news",
      title = item.text,
      text = None,
      url = item.linkHref,
      tags = Seq.empty,
      replace = Map.empty,
      generatedAt = None,
      shouldNotify = true,
      recipients = ActivityRecipients(
        users = Some(recipientUsercodes),
        groups = None
      )
    )
  }
}

object PublishNotification {
  val NEWS_PROVIDER_ID = "news"
}

case class NotificationSave(text: String, linkHref: Option[String])

