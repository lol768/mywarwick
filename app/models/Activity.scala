package models

import controllers.api.SaveMuteRequest
import org.joda.time.DateTime
import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json._
import warwick.sso.Usercode
import play.api.libs.json.Reads.filter
import uk.ac.warwick.util.core.StringUtils

case class ActivityIcon(name: String, colour: Option[String])
object ActivityIcon {
  implicit val writes = Json.writes[ActivityIcon]
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
  publishedAt: DateTime,
  createdAt: DateTime,
  shouldNotify: Boolean,
  audienceId: Option[String] = None,
  publisherId: Option[String] = None,
  sendEmail: Option[Boolean] = None
)

object Activity {
  implicit val writes = Json.writes[Activity]
}

object ActivityRender {

  import DateFormats.isoDateWrites

  implicit val writes: Writes[ActivityRender] = new Writes[ActivityRender] {
    override def writes(o: ActivityRender): JsValue = {
      val json = Json.obj(
        "id" -> o.activity.id,
        "notification" -> o.activity.shouldNotify,
        "provider" -> o.activity.providerId,
        "providerDisplayName" -> o.provider.displayName,
        "type" -> o.activity.`type`,
        "typeDisplayName" -> o.`type`.displayName,
        "title" -> o.activity.title,
        "text" -> o.activity.text,
        "url" -> o.activity.url,
        "tags" -> o.tags,
        "date" -> o.activity.publishedAt
      )

      o.icon match {
        case Some(icon) => json ++ Json.obj("icon" -> Json.toJson(icon))
        case None => json
      }
    }
  }
}

sealed trait ActivityRenderFields {
  val activity: Activity
  val icon: Option[ActivityIcon]
  val tags: Seq[ActivityTag]
  val provider: ActivityProvider
  val `type`: ActivityType
}

case class ActivityRender(
  activity: Activity,
  icon: Option[ActivityIcon],
  tags: Seq[ActivityTag],
  provider: ActivityProvider,
  `type`: ActivityType
) extends ActivityRenderFields

object ActivityRenderWithAudience {
  def applyWithAudience(activityRender: ActivityRender, audienceSize: AudienceSize, audience: Audience, sentCount: Int) =
    ActivityRenderWithAudience(
      activity = activityRender.activity,
      icon = activityRender.icon,
      tags = activityRender.tags,
      provider = activityRender.provider,
      `type` = activityRender.`type`,
      audienceSize = audienceSize,
      audienceComponents = audience.components,
      sentCount = sentCount
    )
}

case class ActivityRenderWithAudience(
  activity: Activity,
  icon: Option[ActivityIcon],
  tags: Seq[ActivityTag],
  provider: ActivityProvider,
  `type`: ActivityType,
  audienceSize: AudienceSize,
  audienceComponents: Seq[Audience.Component],
  sentCount: Int
) extends ActivityRenderFields {
  def isSendingNow = !activity.publishedAt.isAfterNow && audienceSize.toOption.exists(as => sentCount < as)
}

object ActivityTag {
  implicit val reads: Reads[ActivityTag] =
    ((__ \ "name").read[String] and
      (__ \ "display_name").readNullable[String] and
      __.read[TagValue]((
        (__ \ "value").read[String] and
          (__ \ "display_value").readNullable[String]
        ) (TagValue))
      ) (ActivityTag.apply _)

  implicit val writes: Writes[ActivityTag] = new Writes[ActivityTag] {
    override def writes(tag: ActivityTag): JsValue = Json.obj(
      "name" -> tag.name,
      "display_name" -> tag.displayName,
      "value" -> tag.value.internalValue,
      "display_value" -> JsString(tag.value.displayValue.getOrElse(tag.value.internalValue))
    )
  }
}

case class ActivityTag(
  name: String,
  displayName: Option[String],
  value: TagValue
)

case class TagValue(internalValue: String, displayValue: Option[String] = None)

case class ActivityProvider(id: String, sendEmail: Boolean, displayName: Option[String] = None)

case class ActivityType(name: String, displayName: Option[String] = None)

case class ActivitySave(
  changedBy: Usercode,
  publisherId: String,
  providerId: String,
  shouldNotify: Boolean,
  `type`: String,
  title: String,
  text: Option[String] = None,
  url: Option[String] = None,
  tags: Seq[ActivityTag] = Seq.empty,
  replace: Map[String, String] = Map.empty,
  publishedAt: Option[DateTime] = None,
  sendEmail: Option[Boolean] = None
)

object ActivitySave {
  def fromApi(usercode: Usercode, publisherId: String, providerId: String, shouldNotify: Boolean, data: IncomingActivityData): ActivitySave = {
    import data._
    ActivitySave(
      changedBy = usercode,
      publisherId = publisherId,
      providerId = providerId,
      shouldNotify = shouldNotify,
      `type` = `type`,
      title = title,
      text = text,
      url = url,
      tags = tags.getOrElse(Seq.empty),
      replace = replace.getOrElse(Map.empty),
      publishedAt = generated_at,
      sendEmail = send_email
    )
  }
}

case class ActivityRecipients(
  users: Option[Seq[String]],
  groups: Option[Seq[String]]
)

object ActivityRecipients {
  implicit val readsActivityRecipients: Reads[ActivityRecipients] = (
      (__ \ "users").readNullable[Seq[String]](filter(ValidationError("All usercodes must be non-empty"))(_.forall(StringUtils.hasText))) and
      (__ \ "groups").readNullable[Seq[String]](filter(ValidationError("All group names must be non-empty"))(_.forall(StringUtils.hasText)))
    )(ActivityRecipients.apply _)
}

case class IncomingActivityData(
  `type`: String,
  title: String,
  text: Option[String],
  url: Option[String],
  tags: Option[Seq[ActivityTag]],
  replace: Option[Map[String, String]],
  generated_at: Option[DateTime],
  recipients: ActivityRecipients,
  send_email: Option[Boolean]
)

object IncomingActivityData {
  import DateFormats.isoDateReads
  implicit val readsIncomingActivityData = Json.reads[IncomingActivityData]
}

case class ActivityMute(
  usercode: Usercode,
  createdAt: DateTime,
  expiresAt: Option[DateTime],
  activityType: Option[String],
  providerId: Option[String],
  tags: Seq[ActivityTag]
) {
  def matchesTags(matchTags: Seq[ActivityTag]): Boolean = {
    tags.isEmpty || matchTags.isEmpty ||
      tags.forall(tag => matchTags.exists(matchTag =>
        matchTag.name == tag.name && matchTag.value.internalValue == tag.value.internalValue
      ))
  }
}

object ActivityMute {
  import DateFormats.isoDateWrites

  implicit val writes: Writes[ActivityMute] = new Writes[ActivityMute] {
    override def writes(mute: ActivityMute): JsValue = Json.obj(
      "usercode" -> mute.usercode.string,
      "createdAt" -> mute.createdAt,
      "expiresAt" -> mute.expiresAt,
      "activityType" -> mute.activityType,
      "providerId" -> mute.providerId,
      "tags" -> mute.tags
    )
  }
}

case class ActivityMuteRender(
  id: String,
  usercode: Usercode,
  createdAt: DateTime,
  expiresAt: Option[DateTime],
  activityType: Option[ActivityType],
  provider: Option[ActivityProvider],
  tags: Seq[ActivityTag]
)

object ActivityMuteRender {
  import DateFormats.isoDateWrites

  implicit val writes: Writes[ActivityMuteRender] = new Writes[ActivityMuteRender] {
    override def writes(mute: ActivityMuteRender): JsValue = Json.obj(
      "id" -> mute.id,
      "usercode" -> mute.usercode.string,
      "createdAt" -> mute.createdAt,
      "expiresAt" -> mute.expiresAt,
      "activityType" -> mute.activityType.map(activityType => Json.obj(
        "name" -> activityType.name,
        "displayName" -> activityType.displayName
      )),
      "provider" -> mute.provider.map(provider => Json.obj(
        "id" -> provider.id,
        "displayName" -> provider.displayName
      )),
      "tags" -> mute.tags
    )
  }

  def fromActivityMuteSave(id: String, activityMute: ActivityMuteSave) = ActivityMuteRender(
    id,
    activityMute.usercode,
    DateTime.now,
    activityMute.expiresAt,
    activityMute.activityType.map(ActivityType(_)),
    activityMute.providerId.map(ActivityProvider(_, sendEmail = false)),
    activityMute.tags
  )
}

case class ActivityMuteSave(
  usercode: Usercode,
  expiresAt: Option[DateTime],
  activityType: Option[String],
  providerId: Option[String],
  tags: Seq[ActivityTag]
)

object ActivityMuteSave {
  def fromRequest(request: SaveMuteRequest, usercode: Usercode): ActivityMuteSave = ActivityMuteSave(
    usercode = usercode,
    expiresAt = request.expiresAt,
    activityType = request.activityType,
    providerId = request.providerId,
    tags = request.tags
  )
}
