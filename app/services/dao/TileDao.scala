package services.dao

import java.sql.Connection

import anorm.SqlParser._
import anorm._
import com.google.inject.{ImplementedBy, Inject}
import models._
import org.joda.time.DateTime
import play.api.libs.json.{JsObject, Json}
import warwick.anorm.converters.ColumnConversions._

@ImplementedBy(classOf[TileDaoImpl])
trait TileDao {

  def getTilesByIds(usercode: String, ids: Seq[String], groups: Set[String])(implicit c: Connection): Seq[TileInstance]

  def getTilesForUser(usercode: String, groups: Set[String])(implicit c: Connection): Seq[TileInstance]

  def getTilesForAnonymousUser(implicit c: Connection): Seq[TileInstance]

  def saveTilePreferences(usercode: String, tileId: String, preferences: JsObject)(implicit c: Connection): Unit

  def saveTileLayoutForUser(usercode: String, tileLayout: UserTileLayout)(implicit c: Connection): Unit

  def getDefaultTilesForGroups(groups: Set[String], ids: Seq[String] = Nil)(implicit c: Connection): Seq[TileInstance]

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
    getDefaultTilesForGroups(Set("anonymous"))

  private def getUsersAndDefaultTiles(usercode: String, ids: Seq[String], groups: Set[String])(implicit c: Connection): Seq[TileInstance] = {

    val idRestriction = if (ids.isEmpty) "" else "AND ID IN ({ids})"

    val defaultTiles = getDefaultTilesForGroups(groups, ids)

    val userTiles = SQL(
      s"""
         |SELECT ID, TILE_TYPE, TITLE, ICON, DEFAULT_SIZE, DEFAULT_POSITION, COLOUR, FETCH_URL, TILE_POSITION, TILE_SIZE, REMOVED, PREFERENCES
         |FROM USER_TILE JOIN TILE ON ID = TILE_ID
         |WHERE USERCODE = {usercode} $idRestriction ORDER BY TILE_POSITION ASC
      """.stripMargin)
      .on(
        'usercode -> usercode,
        'ids -> ids
      ).as(userTileParser.*)

    val defaultsNotOverridden = defaultTiles.filterNot(dt => userTiles.map(_.tile.id).contains(dt.tile.id))
    (defaultsNotOverridden ++ userTiles.filterNot(_.removed)).sortBy(_.position)
  }

  override def getDefaultTilesForGroups(groups: Set[String], ids: Seq[String] = Nil)(implicit c: Connection): Seq[TileInstance] = {
    val idRestriction = if (ids.isEmpty) "" else "AND ID IN ({ids})"
    SQL(
      s"""
         |SELECT ID, TILE_TYPE, TITLE, ICON, DEFAULT_SIZE, DEFAULT_POSITION, COLOUR, FETCH_URL, DEFAULT_POSITION AS TILE_POSITION, DEFAULT_SIZE AS TILE_SIZE, 0 AS REMOVED, NULL AS PREFERENCES
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
      get[String]("TILE_TYPE") ~
      get[String]("DEFAULT_SIZE") ~
      get[Int]("DEFAULT_POSITION") ~
      get[Int]("COLOUR") ~
      get[String]("FETCH_URL") ~
      get[Int]("TILE_POSITION") ~
      get[String]("TILE_SIZE") ~
      get[String]("TITLE") ~
      get[Option[String]]("ICON") ~
      get[Boolean]("REMOVED") ~
      get[Option[String]]("PREFERENCES") map {
      case tileId ~ tileType ~ defaultSize ~ defaultPosition ~ colour ~ fetchUrl ~ position ~ size ~ title ~ icon ~ removed ~ preferences =>
        TileInstance(
          Tile(tileId, tileType, TileSize.withName(defaultSize), defaultPosition, colour, fetchUrl, title, icon),
          position, TileSize.withName(size),
          preferences.map(Json.parse(_).as[JsObject]),
          removed
        )
    }
  }

  override def saveTilePreferences(usercode: String, tileId: String, preferences: JsObject)(implicit c: Connection) =
    SQL("UPDATE USER_TILE SET PREFERENCES = {preferences} WHERE USERCODE = {usercode} AND TILE_ID = {tileId}")
      .on(
        'preferences -> preferences.toString,
        'usercode -> usercode,
        'tileId -> tileId
      )
      .execute()

  override def saveTileLayoutForUser(usercode: String, tileLayout: UserTileLayout)(implicit c: Connection): Unit = {
    SQL("DELETE USER_TILE WHERE USERCODE = {usercode}")
      .on('usercode -> usercode)
      .execute()

    tileLayout.tiles.zipWithIndex.foreach {
      case (tile, index) =>
        SQL("INSERT INTO USER_TILE (USERCODE, TILE_ID, TILE_POSITION, TILE_SIZE, PREFERENCES, REMOVED, CREATED_AT, UPDATED_AT) VALUES ({usercode}, {tileId}, {tilePosition}, {tileSize}, {preferences}, {removed}, {createdAt}, {updatedAt})")
          .on(
            'usercode -> usercode,
            'tileId -> tile.tileId,
            'tilePosition -> index,
            'tileSize -> tile.size.toString,
            'preferences -> tile.preferences.map(_.toString).orNull,
            'removed -> tile.removed,
            'createdAt -> DateTime.now,
            'updatedAt -> DateTime.now
          )
          .execute()
    }
  }
}