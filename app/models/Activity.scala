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

case class IncomingActivity(
  providerId: String,
  `type`: String,
  title: String,
  text: String,
  tags: Map[String, String],
  replace: Map[String, String],
  generatedAt: Option[DateTime]
)

object Activity {

}

