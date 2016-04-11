package models

import models.TileSize.TileSize
import play.api.libs.json._

case class TileInstance(
  tile: Tile,
  positionMobile: Int,
  positionDesktop: Int,
  size: TileSize,
  preferences: Option[JsObject],
  removed: Boolean
)

case class UserTileSetting(
  id: String,
  size: TileSize,
  preferences: Option[JsObject],
  removed: Boolean = false,
  positionMobile: Int,
  positionDesktop: Int
)

object UserTileSetting {
  implicit val format = Json.format[UserTileSetting]

  def removed(tileId: String): UserTileSetting = {
    UserTileSetting(
      tileId,
      TileSize.small,
      None,
      removed = true,
      0,
      0
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
        "removed" -> o.removed,
        "positionMobile" -> o.positionMobile,
        "positionDesktop" -> o.positionDesktop
      )
  }
}

case class TileLayout(
  tiles: Seq[TileInstance]
)

object TileLayout {
  /**
    * The JSON serialization for TileLayout just reads and writes the tiles array,
    * so I'm not sure why it's a separate object. It would be difficult to add new
    * properties to TileLayout from here.
    */
  implicit val reads = JsPath.read[Seq[TileInstance]].map(TileLayout.apply)

  implicit val writes: Writes[TileLayout] = new Writes[TileLayout] {
    def writes(tileLayout: TileLayout): JsValue = Json.toJson(tileLayout.tiles)
  }
}
