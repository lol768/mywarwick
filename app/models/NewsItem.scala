package models

import org.joda.time.DateTime

case class NewsItem(
  source: String,
  id: String,
  title: String,
  url: String,
  content: String,
  publicationDate: DateTime
)
