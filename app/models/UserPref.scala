package models

import org.joda.time.DateTime
import play.api.libs.json.Json

case class UserTile(
  tile: Tile,
  tileConfig: TileConfig,
  createdAt: DateTime,
  lastChanged: DateTime
)

object UserTile {
  implicit val userTileFormat = Json.format[UserTile]
}

case class UserPref(
tiles: Seq[UserTile]
)

object UserPref {
  implicit val userPrefFormat = Json.format[UserPref]
}


