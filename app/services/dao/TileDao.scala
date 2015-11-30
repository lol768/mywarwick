package services.dao

import java.sql.Connection

import anorm.SqlParser._
import anorm._
import com.google.inject.{ImplementedBy, Inject}
import models._
import org.joda.time.DateTime
import warwick.anorm.converters.ColumnConversions._

@ImplementedBy(classOf[TileDaoImpl])
trait TileDao {

  def getTilesByIds(ids: Seq[String])(implicit c: Connection): Seq[UserTile]

  def getTilesForUser(usercode: String)(implicit c: Connection): Seq[UserTile]

  def getDefaultTilesConfig(implicit c: Connection): Seq[UserTile]

}

class TileDaoImpl @Inject()() extends TileDao {

  override def getTilesByIds(ids: Seq[String])(implicit c: Connection): Seq[UserTile] =
    if (ids.isEmpty)
      Seq.empty
    else
      SQL("SELECT id, type, default_size, fetch_url, tile_position, tile_size, created_at, updated_at FROM user_tile_pref JOIN tile ON tile.id = tile_id WHERE tile_id in ({ids})")
        .on('ids -> ids)
        .as(userTileParser.*)

  override def getTilesForUser(usercode: String)(implicit c: Connection): Seq[UserTile] =
    SQL("SELECT id, type, default_size, fetch_url, tile_position, tile_size, created_at, updated_at FROM user_tile_pref JOIN tile ON tile.id = tile_id WHERE usercode = {usercode}")
      .on('usercode -> usercode)
      .as(userTileParser.*)

  override def getDefaultTilesConfig(implicit c: Connection): Seq[UserTile] =
  //TODO: define collection of default tiles, and return them here
    SQL("SELECT id, type, default_size, fetch_url, tile_position, tile_size, created_at, updated_at FROM user_tile_pref JOIN tile ON tile.id = tile_id")
      .as(userTileParser.*)

  def userTileParser: RowParser[UserTile] = {
    get[String]("ID") ~
      get[String]("TYPE") ~
      get[String]("DEFAULT_SIZE") ~
      get[String]("FETCH_URL") ~
      get[Int]("TILE_POSITION") ~
      get[String]("TILE_SIZE") ~
      get[DateTime]("CREATED_AT") ~
      get[DateTime]("UPDATED_AT") map {
      case id ~ tileType ~ defaultSize ~ fetchUrl ~ position ~ size ~ createdAt ~ updatedAt =>
        UserTile(
          Tile(id, tileType, TileSize.withName(size), fetchUrl), TileConfig(position, TileSize.withName(size)), None, createdAt, updatedAt
        )
    }
  }
}