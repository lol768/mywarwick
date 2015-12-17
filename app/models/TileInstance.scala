package models

import play.api.libs.json._

case class TileInstance(
  tile: Tile,
  tileConfig: TileConfig,
  options: Option[JsObject],
  removed: Boolean
)

object TileInstance {
  //  implicit val format = Json.format[TileInstance]
  implicit val tileInstanceReads = Json.reads[TileInstance]
  implicit val tileInstanceWrites = Json.writes[TileInstance]

  lazy val tileInstanceWritesDigest: Writes[TileInstance] = clientFriendlyWrites

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
  implicit val tileLayoutReads = Json.reads[TileLayout]
  implicit val tileLayoutWrites = Json.writes[TileLayout]

  lazy val tileLayoutWritesDigest: Writes[TileLayout] = new Writes[TileLayout] {
    def writes(tileLayout: TileLayout): JsValue = {
      import TileInstance.tileInstanceWritesDigest
      implicit val tileInstanceWrites = tileInstanceWritesDigest
      JsArray(tileLayout.tiles.map(tileInstance => Json.toJson(tileInstance)))
    }
  }
}