package models

import play.api.libs.json._

case class TileInstance(
  tile: Tile,
  preferences: Option[JsObject],
  removed: Boolean
)

case class UserTileSetting(
  id: String,
  preferences: Option[JsObject],
  removed: Boolean = false
)

object UserTileSetting {
  implicit val format = Json.format[UserTileSetting]

  def removed(tileId: String): UserTileSetting = {
    UserTileSetting(
      tileId,
      None,
      removed = true
    )
  }
}

object TileInstance {
  implicit val reads = Json.reads[TileInstance]

  implicit val writes: Writes[TileInstance] = new Writes[TileInstance] {
    override def writes(o: TileInstance): JsValue =
      Json.obj(
        "id" -> o.tile.id,
        "colour" -> o.tile.colour,
        "icon" -> o.tile.icon,
        "preferences" -> o.preferences,
        "title" -> o.tile.title,
        "type" -> o.tile.tileType,
        "removed" -> o.removed
      )
  }
}
