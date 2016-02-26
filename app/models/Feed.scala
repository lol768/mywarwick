package models

import org.joda.time.DateTime

case class Feed(
  title: String,
  items: Seq[FeedItem]
)

case class FeedItem(
  id: String,
  title: String,
  url: String,
  content: String,
  publicationDate: DateTime
) {

  def asNewsItem(source: NewsSource): NewsItem = NewsItem(source, id, title, url, content, publicationDate)

}
