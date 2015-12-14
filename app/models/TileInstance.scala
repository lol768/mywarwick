package models

import play.api.libs.json._

case class TileInstance(
  tile: Tile,
  tileConfig: TileConfig,
  options: Option[JsObject],
  removed: Boolean
)

object TileInstance {
  implicit val tileInstanceFormat = Json.format[TileInstance]

  // custom Writes omits some values we don't use and makes json more readable to client
  def clientFriendlyWrites: Writes[TileInstance] =
    new Writes[TileInstance] {
      override def writes(tileInstance: TileInstance): JsValue =
        Json.obj(
          "id" -> tileInstance.tile.id,
          "tileType" -> tileInstance.tile.tileType,
          "defaultSize" -> tileInstance.tile.defaultSize,
          "size" -> tileInstance.tileConfig.size,
          "options" -> tileInstance.options
        )
    }
}

case class TileLayout(
  tiles: Seq[TileInstance]
)

object TileLayout {
  implicit val format = Json.format[TileLayout]
}