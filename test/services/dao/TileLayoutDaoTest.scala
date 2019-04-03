package services.dao

import anorm.SQL
import helpers.OneStartAppPerSuite
import models.TileLayout
import helpers.BaseSpec

class TileLayoutDaoTest extends BaseSpec with OneStartAppPerSuite {

  val tileLayoutDao = app.injector.instanceOf[TileLayoutDao]

  "TileLayoutDao" should {

    "get default tile layout for anonymous user" in transaction { implicit c =>

      SQL(
        """
      INSERT INTO TILE (ID, TILE_TYPE, COLOUR, FETCH_URL, TITLE, ICON) VALUES
        ('tile', 'count', 1, 'http://provider', 'Printer Credit', 'print'),
        ('other-tile', 'count', 2, 'http://provider', 'Mail', 'envelope');
      INSERT INTO TILE_GROUP (TILE_ID, "GROUP") VALUES
        ('tile', 'anonymous'),
        ('other-tile', 'anonymous');
      INSERT INTO TILE_GROUP_LAYOUT (GROUP_ID, TILE_ID, LAYOUT_WIDTH, X, Y, WIDTH, HEIGHT) VALUES
        ('anonymous', 'tile', '4', '0', '1', '1', '1'),
        ('anonymous', 'other-tile', '4', '0', '2', '1', '1');
        """).execute()

      val tiles = tileLayoutDao.getDefaultTileLayoutForGroup("anonymous")

      tiles must contain(TileLayout("other-tile", 4, 0, 2, 1, 1))
      tiles must contain(TileLayout("tile", 4, 0, 1, 1, 1))

    }

    "save tile layout for user" in transaction { implicit c =>

      SQL(
        """
      INSERT INTO TILE (ID, TILE_TYPE, COLOUR, FETCH_URL, TITLE, ICON) VALUES
        ('tile', 'count', 1, 'http://provider', 'Printer Credit', 'print'),
        ('other-tile', 'count', 2, 'http://provider', 'Mail', 'envelope');
        """
      ).execute()

      tileLayoutDao.saveTileLayoutForUser(
        "awesome-o",
        Seq(
          TileLayout("other-tile", 4, 0, 2, 1, 1),
          TileLayout("tile", 4, 0, 1, 1, 1)
        ))

      val tiles = tileLayoutDao.getTileLayoutForUser("awesome-o")

      tiles must contain(TileLayout("other-tile", 4, 0, 2, 1, 1))
      tiles must contain(TileLayout("tile", 4, 0, 1, 1, 1))
    }

    "return empty List for user with no layout" in transaction { implicit c =>
      val tiles = tileLayoutDao.getTileLayoutForUser("johnny-5")

      tiles must be ('empty)
    }

  }
}
