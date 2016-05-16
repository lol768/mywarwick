package models

import org.joda.time.DateTime
import play.api.libs.json.Json

// TODO reconcile with the new news stuff
case class NewsItem(
  source: NewsSource,
  id: String,
  title: String,
  url: Option[String],
  content: String,
  publicationDate: DateTime
)

object NewsItem {
  implicit val writes = Json.writes[NewsItem]
}