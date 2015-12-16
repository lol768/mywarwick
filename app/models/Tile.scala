package models

import models.TileSize.TileSize
import play.api.libs.json._

// util for reading/writing JSON enums
object EnumUtils {

  implicit def enumFormat[E <: Enumeration](enum: E): Format[E#Value] =
    Format(enumReads(enum), enumWrites)

  def enumReads[E <: Enumeration](enum: E): Reads[E#Value] = new Reads[E#Value] {
    def reads(json: JsValue): JsResult[E#Value] = json match {
      case JsString(s) =>
        try {
          JsSuccess(enum.withName(s))
        } catch {
          case _: NoSuchElementException => JsError(s"Enumeration expected of type: '${enum.getClass}', but it does not appear to contain the value: '$s'")
        }
      case _ => JsError("String value expected")
    }
  }

  def enumWrites[E <: Enumeration]: Writes[E#Value] = new Writes[E#Value] {
    def writes(v: E#Value): JsValue = JsString(v.toString)
  }
}

object TileSize extends Enumeration {
  type TileSize = Value
  val small, tall, large, wide = Value
}

case class TileConfig(
  position: Int,
  size: TileSize
)

object TileConfig {
  implicit val tileSizeFormat = EnumUtils.enumFormat(TileSize)
  implicit val tileConfigFormat = Json.format[TileConfig]
}

case class Tile(
  id: String,
  tileType: String,
  defaultSize: TileSize,
  defaultPosition: Int,
  fetchUrl: String
)


object Tile {
  implicit val tileSizeFormat = EnumUtils.enumFormat(TileSize)
  implicit val tileFormat = Json.format[Tile]
}


