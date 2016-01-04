package models

import play.api.libs.json._

case class TileInstance(
  tile: Tile,
  tileConfig: TileConfig,
  options: Option[JsObject],
  removed: Boolean
)

object TileInstance {
  implicit val reads = Json.reads[TileInstance]

  implicit val writes: Writes[TileInstance] = new Writes[TileInstance] {
    override def writes(tileInstance: TileInstance): JsValue =
      Json.obj(
        "id" -> tileInstance.tile.id,
        "tileType" -> tileInstance.tile.tileType,
        "defaultSize" -> tileInstance.tile.defaultSize,
        "size" -> tileInstance.tileConfig.size,
        "options" -> tileInstance.options,
        "title" -> tileInstance.tile.title,
        "icon" -> tileInstance.tile.icon
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