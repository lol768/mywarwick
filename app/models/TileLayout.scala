package models

import play.api.libs.json.Json

case class TileLayout(
  tile: String,
  layoutWidth: Int,
  x: Int,
  y: Int,
  width: Int,
  height: Int
)

object TileLayout {
  implicit val format = Json.format[TileLayout]
}
