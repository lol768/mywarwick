package models

case class UserTileLayout(
  usercode: String,
  tileId: String,
  layoutWidth: Int,
  x: Int,
  y: Int,
  width: Int,
  height: Int
)