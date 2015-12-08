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

  def getTilesByIds(usercode: String, ids: Seq[String])(implicit c: Connection): Seq[UserTile]

  def getTilesForUser(usercode: String)(implicit c: Connection): Seq[UserTile]

  def getDefaultTilesConfig(implicit c: Connection): Seq[UserTile]

}

class TileDaoImpl @Inject()() extends TileDao {

  override def getTilesByIds(usercode: String, ids: Seq[String])(implicit c: Connection): Seq[UserTile] =
    if (ids.isEmpty)
      Seq.empty
    else
      SQL("SELECT ID, DEFAULT_SIZE, FETCH_URL, TILE_POSITION, TILE_SIZE, CREATED_AT, UPDATED_AT FROM USER_TILE JOIN TILE ON TILE.ID = TILE_ID WHERE ID IN ({ids}) AND USERCODE = {usercode}")
        .on(
          'usercode -> usercode,
          'ids -> ids
        )
        .as(userTileParser.*)

  override def getTilesForUser(usercode: String)(implicit c: Connection): Seq[UserTile] =
    SQL("SELECT ID, DEFAULT_SIZE, FETCH_URL, TILE_POSITION, TILE_SIZE, CREATED_AT, UPDATED_AT FROM USER_TILE JOIN TILE ON TILE.ID = TILE_ID WHERE USERCODE = {usercode} ORDER BY TILE_POSITION ASC")
      .on('usercode -> usercode)
      .as(userTileParser.*)

  override def getDefaultTilesConfig(implicit c: Connection): Seq[UserTile] =
    //TODO: define collection of default tiles, and return them here
    SQL("SELECT ID, DEFAULT_SIZE, FETCH_URL, TILE_POSITION, TILE_SIZE, CREATED_AT, UPDATED_AT FROM USER_TILE JOIN TILE ON TILE.ID = TILE_ID WHERE USERCODE IS NULL")
      .as(userTileParser.*)

  def userTileParser: RowParser[UserTile] = {
    get[String]("ID") ~
      get[String]("DEFAULT_SIZE") ~
      get[String]("FETCH_URL") ~
      get[Int]("TILE_POSITION") ~
      get[String]("TILE_SIZE") ~
      get[DateTime]("CREATED_AT") ~
      get[DateTime]("UPDATED_AT") map {
      case tileId ~ defaultSize ~ fetchUrl ~ position ~ size ~ createdAt ~ updatedAt =>
        UserTile(
          Tile(tileId, TileSize.withName(defaultSize), fetchUrl),
          TileConfig(position, TileSize.withName(size)),
          None, createdAt, updatedAt
        )
    }
  }
}