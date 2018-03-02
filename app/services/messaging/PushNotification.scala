package services.messaging

import models.{ActivityRecipients, ActivityTag, DateFormats}
import org.joda.time.DateTime
import play.api.libs.json._

import scala.concurrent.duration.FiniteDuration

case class Payload(title: String, text: Option[String], url: Option[String])

case class Priority(value: String)
object Priority {
  val HIGH: Priority = Priority("high")
  val NORMAL: Priority = Priority("normal")
  private val values = Set(HIGH, NORMAL)
  def parse(priority: String): Priority = values.find(_.value == priority).getOrElse(throw new IllegalArgumentException(priority))
  implicit val format: Format[Priority] = new Format[Priority] {
    def reads(json: JsValue) = JsSuccess(parse(json.as[String]))
    def writes(priority: Priority) = JsString(priority.value)
  }
}

object Emoji {
  val ARROW = "↗️"
  val LINK = "🔗"
}

case class PushNotification(
  id: String, // the activityId
  payload: Payload,
  publisherId: Option[String],
  providerId: String,
  notificationType: String,
  ttl: Option[FiniteDuration] = None,
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
  ttl: Option[Int],
  channel: Option[String],
  priority: Option[Priority]
)

object IncomingTransientPushData {
  import DateFormats.isoDateReads
  implicit val reads: Reads[IncomingTransientPushData] = Json.reads[IncomingTransientPushData]
}




