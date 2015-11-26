package models

import play.api.libs.json.Json

case class TileConfig(
  position: Int,
  size: String,
  fetchUrl: String
)

object TileConfig {
  implicit val tileConfigFormat = Json.format[TileConfig]
}

case class Tile(
  id: String,
  `type`: String,
  defaultSize: String,
  defaultFetchUrl: String
)

object Tile {
  implicit val tileFormat = Json.format[Tile]
}
