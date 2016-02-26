package models

import org.joda.time.DateTime
import play.api.libs.json.Json

case class NewsItem(
  source: NewsSource,
  id: String,
  title: String,
  url: String,
  content: String,
  publicationDate: DateTime
)

object NewsItem {
  implicit val writes = Json.writes[NewsItem]
}