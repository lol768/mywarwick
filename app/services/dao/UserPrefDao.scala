package services.dao

import anorm.SqlParser._
import anorm._
import com.google.inject.{ImplementedBy, Inject}
import models._
import org.joda.time.DateTime
import play.api.Logger
import play.api.db.{Database, NamedDatabase}
import warwick.anorm.converters.ColumnConversions._

@ImplementedBy(classOf[UserPrefsDaoImpl])
trait UserPrefsDao {
  def getTilesForUser(usercode: String): Seq[UserTile]

}

class UserPrefsDaoImpl @Inject()(@NamedDatabase("default") val db: Database) extends UserPrefsDao {

  def getTilesForUser(usercode: String): Seq[UserTile] =
    db.withConnection { implicit c =>
      // TODO: actually query db
      SQL("SELECT id, type, default_size, default_fetch_url, tile_position, tile_size, fetch_url, created_at, last_changed FROM user_tile_pref JOIN tile ON tile.id = tile_id WHERE usercode = {usercode}")
        .on('usercode -> usercode)
        .as(userTileParser.*)
    }.toSeq

  def userTileParser: RowParser[UserTile] = {
    get[String]("ID") ~
      get[String]("TYPE") ~
      get[String]("DEFAULT_SIZE") ~
      get[String]("DEFAULT_FETCH_URL") ~
      get[Int]("TILE_POSITION") ~
      get[String]("TILE_SIZE") ~
      get[String]("FETCH_URL") ~
      get[DateTime]("CREATED_AT") ~
      get[DateTime]("LAST_CHANGED") map {
      case id ~ tileType ~ defaultSize ~ defaultFetchUrl ~ position ~ size ~ fetchUrl ~ createdAt ~ lastChanged =>
        UserTile(
          Tile(id, tileType, defaultSize, defaultFetchUrl), TileConfig(position, size, fetchUrl), createdAt, lastChanged
        )
    }
  }
}