package models

import play.api.libs.json._
import system.EnumUtils

case class Tile(
  id: String,
  tileType: String,
  colour: Int,
  fetchUrl: Option[String],
  title: String,
  icon: Option[String],
  timeout: Int
)

object Tile {
  implicit val tileFormat = Json.format[Tile]
}
