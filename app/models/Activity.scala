package models

import models.news.NotificationData
import org.joda.time.DateTime
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class ActivityIcon(name: String, colour: Option[String])
object ActivityIcon {
  implicit val writes = Json.writes[ActivityIcon]
}

case class Provider(id: String, displayName: Option[String], activityIcon: Option[ActivityIcon])
object Provider {
  implicit val writes = Json.writes[Provider]
}

case class Activity(
  id: String,
  providerId: String,

  /**
    * > Some sort of filterable name for what sort of thing the activity is about, e.g `coursework-due` or `squash-court-reserved`
    */
  `type`: String,
  title: String,
  text: Option[String],
  url: Option[String],
  replacedBy: Option[String],
  generatedAt: DateTime,
  createdAt: DateTime,
  shouldNotify: Boolean
)

object Activity {
  implicit val writes = Json.writes[Activity]
}

object ActivityResponse {

  import DateFormats.isoDateWrites

  implicit val writes: Writes[ActivityResponse] = new Writes[ActivityResponse] {
    override def writes(o: ActivityResponse): JsValue = {
      val json = Json.obj(
        "id" -> o.activity.id,
        "notification" -> o.activity.shouldNotify,
        "provider" -> o.activity.providerId,
        "type" -> o.activity.`type`,
        "title" -> o.activity.title,
        "text" -> o.activity.text,
        "url" -> o.activity.url,
        "tags" -> o.tags,
        "date" -> o.activity.generatedAt
      )

      o.icon match {
        case Some(icon) => json ++ Json.obj("icon" -> Json.toJson(icon))
        case None => json
      }
    }
  }
}

case class ActivityResponse(
  activity: Activity,
  icon: Option[ActivityIcon],
  tags: Seq[ActivityTag]
)

object ActivityTag {
  implicit val reads: Reads[ActivityTag] =
    ((__ \ "name").read[String] and
      __.read[TagValue]((
        (__ \ "value").read[String] and
          (__ \ "display_value").readNullable[String]
        ) (TagValue))
      ) (ActivityTag.apply _)

  implicit val writes: Writes[ActivityTag] = new Writes[ActivityTag] {
    override def writes(tag: ActivityTag): JsValue = Json.obj(
      "name" -> tag.name,
      "value" -> tag.value.internalValue,
      "display_value" -> JsString(tag.value.displayValue.getOrElse(tag.value.internalValue))
    )
  }
}

case class ActivityTag(
  name: String,
  value: TagValue
)

case class TagValue(internalValue: String, displayValue: Option[String] = None)

case class ActivitySave(
  providerId: String,
  shouldNotify: Boolean,
  `type`: String,
  title: String,
  text: Option[String] = None,
  url: Option[String] = None,
  tags: Seq[ActivityTag] = Seq.empty,
  replace: Map[String, String] = Map.empty,
  generatedAt: Option[DateTime] = None,
  audienceId: Option[String] = None
)

object ActivitySave {
  def fromApi(providerId: String, shouldNotify: Boolean, data: IncomingActivityData): ActivitySave = {
    import data._
    ActivitySave(providerId, shouldNotify, `type`, title, text, url, tags.getOrElse(Seq.empty), replace.getOrElse(Map.empty), generated_at)
  }

  def fromPublisher(item: NotificationData, audienceId: String) =
    ActivitySave(
      providerId = "news",
      `type` = "news",
      title = item.text,
      url = item.linkHref,
      shouldNotify = true,
      audienceId = Some(audienceId)
    )
}

case class ActivityRecipients(
  users: Option[Seq[String]],
  groups: Option[Seq[String]]
)

object ActivityRecipients {
  implicit val readsActivityRecipients = Json.reads[ActivityRecipients]
}

case class IncomingActivityData(
  `type`: String,
  title: String,
  text: Option[String],
  url: Option[String],
  tags: Option[Seq[ActivityTag]],
  replace: Option[Map[String, String]],
  generated_at: Option[DateTime],
  recipients: ActivityRecipients
)

object IncomingActivityData {
  import DateFormats.isoDateReads
  implicit val readsIncomingActivityData = Json.reads[IncomingActivityData]
}
