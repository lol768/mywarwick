package models

import play.api.libs.json.Json

case class UserTileLayout(
  tile: String,
  layoutWidth: Int,
  x: Int,
  y: Int,
  width: Int,
  height: Int
)

object UserTileLayout {
  implicit val format = Json.format[UserTileLayout]
}
