package models

import play.api.libs.json.Json

// TODO reconcile with new news.
// we still need something like this if we're keeping regular
// feedy feeds around for users to subscribe to.
case class NewsSource(
  title: String,
  url: String,
  colour: String
)

object NewsSource {
  implicit val writes = Json.writes[NewsSource]
}
