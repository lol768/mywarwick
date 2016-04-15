package models

import play.api.libs.json._
import system.EnumUtils

case class Tile(
  id: String,
  tileType: String,
  colour: Int,
  fetchUrl: String,
  title: String,
  icon: Option[String]
)

object Tile {
  implicit val tileSizeFormat = EnumUtils.enumFormat(TileSize)
  implicit val tileFormat = Json.format[Tile]
}

object TileSize extends Enumeration {
  implicit val tileSizeFormat = EnumUtils.enumFormat(TileSize)

  type TileSize = Value
  val small, wide, large = Value
}
