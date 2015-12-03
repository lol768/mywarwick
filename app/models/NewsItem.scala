package models

import org.joda.time.DateTime
import play.api.libs.json.{Json, Writes}

case class NewsItem(
  source: String,
  id: String,
  title: String,
  url: String,
  content: String,
  publicationDate: DateTime
)

object NewsItem {
  implicit val writes = Writes { item: NewsItem =>
    Json.obj(
      "id" -> item.id,
      "title" -> item.title,
      "url" -> Json.obj(
        "href" -> item.url
      ),
      "content" -> item.content,
      "publicationDate" -> item.publicationDate.getMillis,
      "source" -> item.source
    )
  }
}