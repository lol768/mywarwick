package models

import org.joda.time.DateTime

case class Activity(
  id: String,
  providerId: String,
  `type`: String,
  title: String,
  text: String,
  replacedBy: Option[String],
  createdAt: DateTime,
  shouldNotify: Boolean
)

case class ActivityTag(
  name: String,
  value: TagValue
)

case class TagValue(internalValue: String, displayValue: Option[String])

case class ActivityResponse(
  id: String,
  notification: Boolean,
  source: String,
  `type`: String,
  title: String,
  text: String,
  tags: Seq[ActivityTag],
  date: DateTime
)

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
