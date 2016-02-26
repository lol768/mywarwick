package models

import play.api.libs.json.Json

case class NewsSource(
  title: String,
  url: String,
  colour: String
)

object NewsSource {
  implicit val writes = Json.writes[NewsSource]
}
