package services.dao

import java.sql.Connection

import anorm.SqlParser._
import anorm._
import com.google.inject.{ImplementedBy, Singleton}
import models.UserTileLayout

import scala.util.Try

@ImplementedBy(classOf[UserTileLayoutDaoImpl])
trait UserTileLayoutDao {

  def getTileLayoutForUser(usercode: String)(implicit c: Connection): Seq[UserTileLayout]

  def saveTileLayoutForUser(usercode: String, layout: Seq[UserTileLayout])(implicit c: Connection): Try[Unit]

}

@Singleton
class UserTileLayoutDaoImpl extends UserTileLayoutDao {

  val userTileLayoutParser: RowParser[UserTileLayout] =
    get[String]("TILE_ID") ~
      get[Int]("LAYOUT_WIDTH") ~
      get[Int]("X") ~
      get[Int]("Y") ~
      get[Int]("WIDTH") ~
      get[Int]("HEIGHT") map {
      case tileId ~ layoutWidth ~ x ~ y ~ width ~ height =>
        UserTileLayout(tileId, layoutWidth, x, y, width, height)
    }

  override def getTileLayoutForUser(usercode: String)(implicit c: Connection): Seq[UserTileLayout] =
    SQL("SELECT TILE_ID, LAYOUT_WIDTH, X, Y, WIDTH, HEIGHT FROM USER_TILE_LAYOUT WHERE USERCODE = {usercode}")
      .on('usercode -> usercode)
      .as(userTileLayoutParser.*)

  override def saveTileLayoutForUser(usercode: String, layout: Seq[UserTileLayout])(implicit c: Connection): Try[Unit] = {
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
