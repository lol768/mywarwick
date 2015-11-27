package models

import org.joda.time.DateTime
import play.api.libs.json.{JsObject, Json}

case class UserTile(
  tile: Tile,
  tileConfig: TileConfig,
  options: Option[JsObject],
  createdAt: DateTime,
  updatedAt: DateTime
)

object UserTile {
  implicit val userTileFormat = Json.format[UserTile]
}

case class UserTileLayout(
  tiles: Seq[UserTile]
)

object UserTileLayout {
  implicit val userPrefFormat = Json.format[UserTileLayout]
}