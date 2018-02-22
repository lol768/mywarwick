package services.messaging

import models.{ActivityRecipients, ActivityTag, DateFormats, IncomingActivityData}
import org.joda.time.DateTime
import play.api.libs.json.{Json, Reads}

case class Payload(title: String, text: Option[String], url: Option[String])

object Emoji {
  val ARROW = "↗️"
  val LINK = "🔗"
}

case class PushNotification(
  payload: Payload,
  publisherId: Option[String],
  providerId: String,
  notificationType: String,
  ttlSeconds: Option[Int] = None,
  fcmSound: Option[String] = None,
  apnsSound: Option[String] = None,
  tag: Option[String] = None
) {
  def buildTitle(emoji: String): String =
    payload.url.map(_ => s"${payload.title} $emoji").getOrElse(payload.title)
}

case class IncomingTransientPushData(
  `type`: String,
  title: String,
  text: Option[String],
  url: Option[String],
  tags: Option[Seq[ActivityTag]],
  replace: Option[Map[String, String]],
  generated_at: Option[DateTime],
  recipients: ActivityRecipients,
  send_email: Option[Boolean],
  tag: Option[String],
  ttlSeconds: Option[Int],
  fcmSound: Option[String],
  apnsSound: Option[String]
)

object IncomingTransientPushData {
  import DateFormats.isoDateReads
  implicit val reads: Reads[IncomingTransientPushData] = Json.reads[IncomingTransientPushData]
}




