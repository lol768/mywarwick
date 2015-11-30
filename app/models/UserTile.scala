package models

import org.joda.time.DateTime
import play.api.libs.json.{JsObject, Json}

case class UserTile(
  id: String,
  tile: Tile,
  tileConfig: TileConfig,
  options: Option[JsObject],
  createdAt: DateTime,
  updatedAt: DateTime
)

object UserTile {
  implicit val userTileFormat = Json.format[UserTile]
}

case class TileLayout(
  tiles: Seq[UserTile]
)

object TileLayout {
  implicit val format = Json.format[TileLayout]
}