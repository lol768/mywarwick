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
  implicit val tileFormat = Json.format[Tile]
}
