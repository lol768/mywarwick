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

  def saveTileConfiguration(usercode: String, tileLayout: Seq[UserTileSetting])(implicit c: Connection): Unit

  def getDefaultTilesForGroups(groups: Set[String], ids: Seq[String] = Nil)(implicit c: Connection): Seq[TileInstance]

  def getAllTiles()(implicit c: Connection): Seq[Tile]

  def getAllUserTiles()(implicit c: Connection): Seq[UserTile]

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
         |SELECT ID, TILE_TYPE, TITLE, ICON, COLOUR, TIMEOUT, FETCH_URL, REMOVED, PREFERENCES
         |FROM USER_TILE JOIN TILE ON ID = TILE_ID
         |WHERE USERCODE = {usercode} $idRestriction
      """.stripMargin)
      .on(
        'usercode -> usercode,
        'ids -> ids
      ).as(userTileParser.*)

    val defaultsNotOverridden = defaultTiles.filterNot(dt => userTiles.map(_.tile.id).contains(dt.tile.id))
    defaultsNotOverridden ++ userTiles
  }

  override def getDefaultTilesForGroups(groups: Set[String], ids: Seq[String] = Nil)(implicit c: Connection): Seq[TileInstance] = {
    if (groups.isEmpty) {
      Nil
    } else {
      val idRestriction = if (ids.isEmpty) "" else "AND ID IN ({ids})"
      SQL(
        s"""
           |SELECT ID, TILE_TYPE, TITLE, ICON, COLOUR, TIMEOUT, FETCH_URL, 0 AS REMOVED, NULL AS PREFERENCES
           |FROM TILE
           |WHERE EXISTS (SELECT * FROM TILE_GROUP WHERE TILE_ID = ID and "GROUP" in ({groups})) $idRestriction
      """.stripMargin).on(
        'ids -> ids,
        'groups -> groups
      ).as(userTileParser.*)
    }
  }

  def userTileParser: RowParser[TileInstance] = {
    get[String]("ID") ~
      get[String]("TILE_TYPE") ~
      get[Int]("COLOUR") ~
      get[Option[String]]("FETCH_URL") ~
      get[String]("TITLE") ~
      get[Option[String]]("ICON") ~
      get[Int]("TIMEOUT") ~
      get[Boolean]("REMOVED") ~
      get[Option[String]]("PREFERENCES") map {
      case tileId ~ tileType ~ colour ~ fetchUrl ~ title ~ icon ~ timeout ~ removed ~ preferences =>
        TileInstance(
          Tile(tileId, tileType, colour, fetchUrl, title, icon, timeout),
          preferences.map(Json.parse(_).as[JsObject]),
          removed
        )
    }
  }

  def tileParser: RowParser[Tile] = {
    get[String]("ID") ~
      get[String]("TILE_TYPE") ~
      get[Int]("COLOUR") ~
      get[Option[String]]("FETCH_URL") ~
      get[String]("TITLE") ~
      get[Option[String]]("ICON") ~
      get[Int]("TIMEOUT") map {
      case tileId ~ tileType ~ colour ~ fetchUrl ~ title ~ icon ~ timeout => {
        Tile(
          tileId,
          tileType,
          colour,
          fetchUrl,
          title,
          icon,
          timeout
        )
      }
    }
  }

  def rawUserTileParser: RowParser[UserTile] = {
    get[String]("USERCODE") ~
    get[String]("TILE_ID") ~
    get[DateTime]("CREATED_AT") ~
    get[DateTime]("UPDATED_AT") ~
    get[Boolean]("REMOVED") ~
    get[Option[String]]("PREFERENCES") map {
      case userCode ~ tileId ~ createdAt ~ updatedAt ~ removed ~ preferences => {
        UserTile(
          userCode,
          tileId,
          createdAt,
          updatedAt,
          removed,
          preferences
        )
      }
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

  override def saveTileConfiguration(usercode: String, tileLayout: Seq[UserTileSetting])(implicit c: Connection): Unit = {
    val update = "UPDATE USER_TILE SET PREFERENCES = {preferences}, REMOVED = {removed}, UPDATED_AT = {now} WHERE USERCODE = {usercode} AND TILE_ID = {id}"
    val insert = "INSERT INTO USER_TILE (USERCODE, TILE_ID, PREFERENCES, REMOVED, CREATED_AT, UPDATED_AT) VALUES ({usercode}, {id}, {preferences}, {removed}, {now}, {now})"

    tileLayout.foreach {
      tile =>
        val params = Seq[NamedParameter](
          'id -> tile.id,
          'now -> DateTime.now,
          'preferences -> tile.preferences.map(_.toString).orNull,
          'removed -> tile.removed,
          'usercode -> usercode
        )

        SQL(update).on(params: _*).executeUpdate() > 0 || SQL(insert).on(params: _*).execute()
    }
  }

  override def getAllTiles()(implicit c: Connection): Seq[Tile] = SQL(s"select * from TILE").as(tileParser.*)

  override def getAllUserTiles()(implicit c: Connection): Seq[UserTile] = SQL(s"select * from USER_TILE").as(rawUserTileParser.*)
}
