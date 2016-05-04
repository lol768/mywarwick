package services.dao

import java.sql.Connection

import anorm.SqlParser._
import anorm._
import com.google.inject.{ImplementedBy, Singleton}
import models.TileLayout

import scala.util.Try

@ImplementedBy(classOf[TileLayoutDaoImpl])
trait TileLayoutDao {

  def getTileLayoutForUser(usercode: String)(implicit c: Connection): Seq[TileLayout]

  def getDefaultTileLayoutForGroup(group: String)(implicit c: Connection): Seq[TileLayout]

  def saveTileLayoutForUser(usercode: String, layout: Seq[TileLayout])(implicit c: Connection): Try[Unit]

}

@Singleton
class TileLayoutDaoImpl extends TileLayoutDao {

  val tileLayoutParser: RowParser[TileLayout] =
    get[String]("TILE_ID") ~
      get[Int]("LAYOUT_WIDTH") ~
      get[Int]("X") ~
      get[Int]("Y") ~
      get[Int]("WIDTH") ~
      get[Int]("HEIGHT") map {
      case tileId ~ layoutWidth ~ x ~ y ~ width ~ height =>
        TileLayout(tileId, layoutWidth, x, y, width, height)
    }

  override def getTileLayoutForUser(usercode: String)(implicit c: Connection): Seq[TileLayout] =
    SQL("SELECT TILE_ID, LAYOUT_WIDTH, X, Y, WIDTH, HEIGHT FROM USER_TILE_LAYOUT WHERE USERCODE = {usercode}")
      .on('usercode -> usercode)
      .as(tileLayoutParser.*)

  override def getDefaultTileLayoutForGroup(group: String)(implicit c: Connection): Seq[TileLayout] =
    SQL("SELECT TILE_ID, LAYOUT_WIDTH, X, Y, WIDTH, HEIGHT FROM TILE_GROUP_LAYOUT WHERE GROUP_ID = {group}")
      .on('group -> group)
      .as(tileLayoutParser.*)

  override def saveTileLayoutForUser(usercode: String, layout: Seq[TileLayout])(implicit c: Connection): Try[Unit] = {
    SQL("DELETE FROM USER_TILE_LAYOUT WHERE USERCODE = {usercode}").on('usercode -> usercode).execute()

    Try {
      layout.foreach { item =>
        SQL("INSERT INTO USER_TILE_LAYOUT (USERCODE, TILE_ID, LAYOUT_WIDTH, X, Y, WIDTH, HEIGHT) VALUES ({usercode}, {tileId}, {layoutWidth}, {x}, {y}, {width}, {height})")
          .on(
            'usercode -> usercode,
            'tileId -> item.tile,
            'layoutWidth -> item.layoutWidth,
            'x -> item.x,
            'y -> item.y,
            'width -> item.width,
            'height -> item.height
          )
          .execute()
      }
    }
  }

}
