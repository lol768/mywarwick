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
  timeout: Option[Int]
)

object Tile {
  implicit val tileFormat = Json.format[Tile]
}
