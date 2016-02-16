package models

import models.TileSize.TileSize
import play.api.libs.json._

case class TileInstance(
  tile: Tile,
  position: Int,
  size: TileSize,
  preferences: Option[JsObject],
  removed: Boolean
)

case class UserTileSetting(
  id: String,
  size: TileSize,
  preferences: Option[JsObject],
  removed: Boolean = false
)

object UserTileSetting {
  implicit val format = Json.format[UserTileSetting]

  def removed(tileId: String): UserTileSetting = {
    UserTileSetting(
      tileId,
      TileSize.small,
      None,
      removed = true
    )
  }
}

case class UserTileLayout(tiles: Seq[UserTileSetting])

object UserTileLayout {
  implicit val format = Json.format[UserTileLayout]
}

object TileInstance {
  implicit val reads = Json.reads[TileInstance]

  implicit val writes: Writes[TileInstance] = new Writes[TileInstance] {
    override def writes(o: TileInstance): JsValue =
      Json.obj(
        "id" -> o.tile.id,
        "colour" -> o.tile.colour,
        "defaultSize" -> o.tile.defaultSize,
        "icon" -> o.tile.icon,
        "preferences" -> o.preferences,
        "size" -> o.size,
        "title" -> o.tile.title,
        "type" -> o.tile.tileType,
        "removed" -> o.removed
      )
  }
}

case class TileLayout(
  tiles: Seq[TileInstance]
)

object TileLayout {
  implicit val reads = Json.reads[TileLayout]

  implicit val writes: Writes[TileLayout] = new Writes[TileLayout] {
    def writes(tileLayout: TileLayout): JsValue = Json.toJson(tileLayout.tiles)
  }
}