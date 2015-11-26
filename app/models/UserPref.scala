package models

import org.joda.time.DateTime

case class UserPref(
  tiles: Seq[UserTile]
)

case class UserTile(
  tile: Tile,
  tileConfig: TileConfig,
  createdAt: DateTime,
  lastChanged: DateTime
)
