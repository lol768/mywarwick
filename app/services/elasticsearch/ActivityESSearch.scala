package services.elasticsearch

import org.joda.time.Interval

object ActivityESSearch {

  case class SearchQuery(
    activity_id: Option[String] = None,
    provider_id: Option[String] = None,
    activity_type: Option[String] = None,
    title: Option[String] = None,
    url: Option[String] = None,
    text: Option[String] = None,
    replaced_by: Option[String] = None,
    publish_at: Option[Interval] = None,
    publisher: Option[String] = None,
    audienceComponents: Option[Seq[String]] = None,
    resolvedUsers: Option[Seq[String]] = None,
    isAlert: Option[Boolean] = None
  )

  case class CountQueryResponse(
    activityCount: Long,
    totalUserCount: Long
  )

  object CountQueryResponse {
    def empty = CountQueryResponse(0, 0)
  }


}

