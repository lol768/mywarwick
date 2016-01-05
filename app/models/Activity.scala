package models

import org.joda.time.DateTime
import play.api.libs.json._
import play.api.libs.functional.syntax._

case class Activity(
  id: String,
  providerId: String,
  /**
    * > Some sort of filterable name for what sort of thing the activity is about, e.g `coursework-due` or `squash-court-reserved`
    * > (I think)
    */
  `type`: String,
  title: String,
  text: String,
  replacedBy: Option[String],
  generatedAt: DateTime,
  createdAt: DateTime,
  shouldNotify: Boolean
)

object ActivityResponse {

  import DateFormats.isoDateWrites

  implicit val writes: Writes[ActivityResponse] = new Writes[ActivityResponse] {
    override def writes(o: ActivityResponse): JsValue = Json.obj(
      "id" -> o.activity.id,
      "notification" -> o.activity.shouldNotify,
      "provider" -> o.activity.providerId,
      "type" -> o.activity.`type`,
      "title" -> o.activity.title,
      "text" -> o.activity.text,
      "tags" -> o.tags,
      "date" -> o.activity.generatedAt
    )
  }
}

case class ActivityResponse(
  activity: Activity,
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

case class ActivityPrototype(
  providerId: String,
  `type`: String,
  title: String,
  text: String,
  tags: Seq[ActivityTag],
  replace: Map[String, String],
  generatedAt: Option[DateTime],
  shouldNotify: Boolean,
  recipients: ActivityRecipients
)

case class ActivityRecipients(
  users: Option[Seq[String]],
  groups: Option[Seq[String]]
)

object ActivityRecipients {
  lazy val empty = ActivityRecipients(None, None)

  implicit val readsActivityRecipients = Json.reads[ActivityRecipients]
}
