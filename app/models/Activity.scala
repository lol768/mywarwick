package models

import org.joda.time.DateTime

case class Activity(
  id: String,
  providerId: String,
  activityType: String,
  title: String,
  text: String,
  replacedBy: String,
  createdAt: DateTime,
  shouldNotify: Boolean
)

case class ActivityPrototype(
  appId: String,
  `type`: String,
  title: String,
  text: String,
  tags: Map[String, String],
  replace: Map[String, String],
  generatedAt: Option[DateTime],
  shouldNotify: Boolean
)

case class PostedActivity(
  `type`: String,
  title: String,
  text: String,
  tags: Option[Map[String, String]],
  replace: Option[Map[String, String]],
  generated_at: Option[DateTime]
) {

  def toActivityPrototype(appId: String, shouldNotify: Boolean): ActivityPrototype =
    ActivityPrototype(
      appId = appId,
      `type` = `type`,
      title = title,
      text = text,
      tags = tags.getOrElse(Map.empty),
      replace = replace.getOrElse(Map.empty),
      generatedAt = generated_at,
      shouldNotify = shouldNotify
    )

}
