package services.elasticsearch

import java.util.Date

case class ActivityESSearchQuery(
  provider_id: Option[String] = None,
  activity_type: Option[String] = None,
  title: Option[String] = None,
  url: Option[String] = None,
  text: Option[String] = None,
  replaced_by: Option[String] = None,
  publish_at: Option[ActivityESSearchQuery.DateRange] = None,
  publisher: Option[String] = None,
  audienceComponents: Option[Seq[String]] = None,
  resolvedUsers: Option[Seq[String]] = None
)

object ActivityESSearchQuery {

  case class DateRange(from: Date, to: Date)

}