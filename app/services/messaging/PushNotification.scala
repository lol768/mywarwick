package services.messaging

import models.{ActivityRecipients, ActivityTag}
import org.joda.time.DateTime
import play.api.libs.json.{Json, Reads}

case class Payload(title: String, text: Option[String], url: Option[String])

case class Priority(value: String)
object Priority {
  val HIGH: Priority = Priority("high")
  val NORMAL: Priority = Priority("normal")
  private val values = Set(HIGH, NORMAL)
  def parse(priority: String): Option[Priority] = values.find(_.value == priority)
  implicit val reads: Reads[Priority] = Json.reads[Priority]
}

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
  tag: Option[String] = None,
  channel: Option[String] = None,
  priority: Option[Priority] = None
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
  apnsSound: Option[String],
  channel: Option[String],
  priority: Option[Priority]
)

object IncomingTransientPushData {
  implicit val reads: Reads[IncomingTransientPushData] = Json.reads[IncomingTransientPushData]
}




