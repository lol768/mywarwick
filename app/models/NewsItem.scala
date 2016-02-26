package models

import org.joda.time.DateTime
import play.api.libs.json.Json.obj
import play.api.libs.json.Writes

case class NewsItem(
  source: NewsSource,
  id: String,
  title: String,
  url: String,
  content: String,
  publicationDate: DateTime
)

object NewsItem {
  implicit val writes = Writes { item: NewsItem =>
    obj(
      "id" -> item.id,
      "title" -> item.title,
      "url" -> obj(
        "href" -> item.url
      ),
      "content" -> item.content,
      "publicationDate" -> item.publicationDate.getMillis,
      "source" -> obj(
        "title" -> item.source.title,
        "colour" -> item.source.colour
      )
    )
  }
}