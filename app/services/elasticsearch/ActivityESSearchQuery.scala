package services.elasticsearch

import java.util.Date

case class ActivityESSearchQuery(
  provider_id: Option[String] = Option.empty,
  activity_type: Option[String] = Option.empty,
  title: Option[String] = Option.empty,
  url: Option[String] = Option.empty,
  text: Option[String] = Option.empty,
  replaced_by: Option[String] = Option.empty,
  publish_at: Option[ActivityESSearchQuery.DateRange] = Option.empty,
  publisher: Option[String] = Option.empty,
  audienceComponents: Option[Seq[String]] = Option.empty,
  resolvedUsers: Option[Seq[String]] = Option.empty
)

object ActivityESSearchQuery {

  case class DateRange(from: Date, to: Date)

}