package services.dao

import java.sql.Connection

import anorm._
import com.google.inject.{ImplementedBy, Inject}
import play.api.libs.json.{JsObject, Json}

@ImplementedBy(classOf[UserTileDaoImpl])
trait UserTileDao {

  def saveTilePreferences(usercode: String, tileId: String, preferences: JsObject)(implicit c: Connection): Unit

  def getTilePreferences(usercode: String, tileId: String)(implicit c: Connection): Option[JsObject]

}

class UserTileDaoImpl @Inject()() extends UserTileDao {

  def saveTilePreferences(usercode: String, tileId: String, preferences: JsObject)(implicit c: Connection) =
    SQL("UPDATE USER_TILE SET PREFERENCES = {preferences} WHERE USERCODE = {usercode} AND TILE_ID = {tileId}")
      .on(
        'preferences -> preferences.toString,
        'usercode -> usercode,
        'tileId -> tileId
      )
      .execute()

  def getTilePreferences(usercode: String, tileId: String)(implicit c: Connection) =
    SQL("SELECT PREFERENCES FROM USER_TILE WHERE USERCODE = {usercode} AND TILE_ID = {tileId}")
      .on(
        'usercode -> usercode,
        'tileId -> tileId
      )
      .as(anorm.SqlParser.scalar[String].singleOpt)
      .map(Json.parse)
      .asInstanceOf[Option[JsObject]]

}