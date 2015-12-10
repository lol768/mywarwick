package services.dao

import java.sql.Connection

import anorm.SqlParser._
import anorm._
import com.google.inject.{ImplementedBy, Inject}
import models._
import warwick.anorm.converters.ColumnConversions._

@ImplementedBy(classOf[TileDaoImpl])
trait TileDao {

  def getTilesByIds(usercode: String, ids: Seq[String], groups: Set[String])(implicit c: Connection): Seq[TileInstance]

  def getTilesForUser(usercode: String, groups: Set[String])(implicit c: Connection): Seq[TileInstance]

  def getTilesForAnonymousUser(implicit c: Connection): Seq[TileInstance]

}

class TileDaoImpl @Inject()() extends TileDao {

  override def getTilesByIds(usercode: String, ids: Seq[String], groups: Set[String])(implicit c: Connection): Seq[TileInstance] =
    if (ids.isEmpty)
      Seq.empty
    else {
      getUsersAndDefaultTiles(usercode, ids, groups)
    }

  override def getTilesForUser(usercode: String, groups: Set[String])(implicit c: Connection): Seq[TileInstance] =
    getUsersAndDefaultTiles(usercode, Nil, groups)

  override def getTilesForAnonymousUser(implicit c: Connection): Seq[TileInstance] =
    getDefaultTilesForGroups(Nil, Set("anonymous"))

  private def getUsersAndDefaultTiles(usercode: String, ids: Seq[String], groups: Set[String])(implicit c: Connection): Seq[TileInstance] = {

    val idRestriction = if (ids.isEmpty) "" else "AND ID IN ({ids})"

    val defaultTiles = getDefaultTilesForGroups(ids, groups)

    val userTiles = SQL(
      s"""
        |SELECT ID, DEFAULT_SIZE, DEFAULT_POSITION, FETCH_URL, TILE_POSITION, TILE_SIZE, REMOVED
        |FROM USER_TILE JOIN TILE ON ID = TILE_ID
        |WHERE USERCODE = {usercode} $idRestriction ORDER BY TILE_POSITION ASC
      """.stripMargin)
      .on(
        'usercode -> usercode,
        'ids -> ids
      ).as(userTileParser.*)

    val defaultsNotOverridden = defaultTiles.filterNot(dt => userTiles.map(_.tile.id).contains(dt.tile.id))
    (defaultsNotOverridden ++ userTiles.filterNot(_.removed)).sortBy(_.tileConfig.position)
  }

  private def getDefaultTilesForGroups(ids: Seq[String], groups: Set[String])(implicit c: Connection): Seq[TileInstance] = {
    val idRestriction = if (ids.isEmpty) "" else "AND ID IN ({ids})"
    SQL(s"""
      |SELECT ID, DEFAULT_SIZE, DEFAULT_POSITION, FETCH_URL, DEFAULT_POSITION AS TILE_POSITION, DEFAULT_SIZE AS TILE_SIZE, 0 AS REMOVED
      |FROM TILE
      |WHERE EXISTS (SELECT * FROM TILE_GROUP WHERE TILE_ID = ID and "GROUP" in ({groups})) $idRestriction
      |ORDER BY DEFAULT_POSITION ASC
    """.stripMargin).on(
      'ids -> ids,
      'groups -> groups
    ).as(userTileParser.*)
  }

  def userTileParser: RowParser[TileInstance] = {
      get[String]("ID") ~
      get[String]("DEFAULT_SIZE") ~
      get[Int]("DEFAULT_POSITION") ~
      get[String]("FETCH_URL") ~
      get[Int]("TILE_POSITION") ~
      get[String]("TILE_SIZE") ~
      get[Boolean]("REMOVED") map {
      case tileId ~ defaultSize ~ defaultPosition ~ fetchUrl ~ position ~ size ~ removed  =>
        TileInstance(
          Tile(tileId, TileSize.withName(defaultSize), defaultPosition, fetchUrl),
          TileConfig(position, TileSize.withName(size)),
          None,
          removed
        )
    }
  }
}