package models

case class TileConfig(
  xPos: Int,
  yPos: Int,
  userDefinedSize: String
)

case class Tile(
  id: String,
  `type`: String,
  defaultSize: String
)
