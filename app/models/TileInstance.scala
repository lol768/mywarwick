package models

import org.joda.time.DateTime
import play.api.libs.json._

case class UserTile(
  tile: Tile,
  tileConfig: TileConfig,
  options: Option[JsObject],
  createdAt: DateTime,
  updatedAt: DateTime
)

object UserTile {
  implicit val userTileFormat = Format(Json.reads[UserTile], clientFriendlyWrites)

  // custom Writes omits some values we don't use and makes json more readable to client
  def clientFriendlyWrites: Writes[UserTile] =
    new Writes[UserTile] {
      override def writes(userTile: UserTile): JsValue =
        Json.obj(
          "id" -> userTile.tile.id,
          "tileType" -> userTile.tile.tileType,
          "defaultSize" -> userTile.tile.defaultSize,
          "size" -> userTile.tileConfig.size,
          "createdAt" -> userTile.createdAt,
          "updatedAt" -> userTile.updatedAt,
          "options" -> userTile.options
        )
    }
}

case class TileLayout(
  tiles: Seq[UserTile]
)

object TileLayout {
  implicit val format = Json.format[TileLayout]
}