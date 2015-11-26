package services.dao

import anorm.SqlParser._
import anorm._
import com.google.inject.{ImplementedBy, Inject}
import models._
import org.joda.time.DateTime
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
      SQL("SELECT id, type, defaultsize, x_pos, y_pos, size, created_at, last_changed FROM user_tile_pref JOIN tile ON TILE_ID = tile.id WHERE usercode == usercode")
      .on('usercode -> usercode)
      .as(userTileParser.*)
    }.toSeq

  def userTileParser: RowParser[UserTile] = {
    get[String]("ID") ~
      get[String]("TYPE") ~
      get[String]("DEFAULT_SIZE") ~
      get[Int]("X_POS") ~
      get[Int]("Y_POS") ~
      get[String]("SIZE") ~
      get[DateTime]("CREATED_AT") ~
      get[DateTime]("LAST_CHANGED") map {
      case id ~ tileType ~ defaultSize ~ xPos ~ yPos ~ userDefinedSize ~ createdAt ~ lastChanged =>
        UserTile(
          Tile(id, tileType, defaultSize), TileConfig(xPos, yPos, userDefinedSize), createdAt, lastChanged
        )
    }
  }

}