package models

import play.api.libs.json.Json

object NewsCategory {
  implicit val format = Json.format[NewsCategory]
}

case class NewsCategory(id: String, name: String)

