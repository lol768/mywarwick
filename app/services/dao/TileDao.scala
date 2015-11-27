package services.dao

import anorm.SqlParser._
import anorm._
import com.google.inject.{ImplementedBy, Inject}
import models._
import org.joda.time.DateTime
import play.api.db.{Database, NamedDatabase}
import warwick.anorm.converters.ColumnConversions._

@ImplementedBy(classOf[TileDaoImpl])
trait TileDao {
  def getTilesForUser(usercode: String): Seq[UserTile]

  def getDefaultTilesConfig: Seq[UserTile]

}

class TileDaoImpl @Inject()(@NamedDatabase("default") val db: Database) extends TileDao {

  def getTilesForUser(usercode: String): Seq[UserTile] =
    db.withConnection { implicit c =>
      SQL("SELECT id, type, default_size, fetch_url, tile_position, tile_size, created_at, updated_at FROM user_tile_pref JOIN tile ON tile.id = tile_id WHERE usercode = {usercode}")
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
      get[DateTime]("CREATED_AT") ~
      get[DateTime]("UPDATED_AT") map {
      case id ~ tileType ~ defaultSize ~ defaultFetchUrl ~ position ~ size ~ createdAt ~ updatedAt =>
        UserTile(
          Tile(id, tileType, TileSize.withName(size), defaultFetchUrl), TileConfig(position, TileSize.withName(size)), None, createdAt, updatedAt
        )
    }
  }

  def getDefaultTilesConfig: Seq[UserTile] =
  //TODO: define collection of default tiles, and return them here
    db.withConnection { implicit c =>
      SQL("SELECT id, type, default_size, fetch_url, tile_position, tile_size, created_at, updated_at FROM user_tile_pref JOIN tile ON tile.id = tile_id")
        .as(userTileParser.*)
    }.toSeq
}