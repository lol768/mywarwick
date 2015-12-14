package models

import org.joda.time.DateTime
import play.api.libs.json.{JsObject, Json}

case class TileInstance(
  tile: Tile,
  tileConfig: TileConfig,
  options: Option[JsObject],
  removed: Boolean
)

object TileInstance {
  implicit val userTileFormat = Json.format[TileInstance]
}

case class TileLayout(
  tiles: Seq[TileInstance]
)

object TileLayout {
  implicit val format = Json.format[TileLayout]
}