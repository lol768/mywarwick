package services.dao

import helpers.OneStartAppPerSuite
import org.scalatestplus.play.PlaySpec

class TileDaoTest extends PlaySpec with OneStartAppPerSuite {

  val tileDao = app.injector.instanceOf[TileDao]

  "TileDao" should {

    "get tiles for user ordered by position" in transaction { implicit c =>

      anorm.SQL(
        """
      INSERT INTO TILE (ID, TILE_TYPE, DEFAULT_SIZE, FETCH_URL) VALUES
        ('tile', 'list', 'large', 'http://provider'),
        ('other-tile', 'text', 'wide', 'http://provider');
      INSERT INTO USER_TILE (USERCODE, TILE_ID, TILE_POSITION, TILE_SIZE, CREATED_AT, UPDATED_AT) VALUES
        ('someone', 'tile', 1, 'large', SYSDATE, SYSDATE),
        ('someone', 'other-tile', 0, 'large', SYSDATE, SYSDATE);
        """).execute()

      val tiles = tileDao.getTilesForUser("someone")

      tiles.map(_.tile.id) must equal(Seq("other-tile", "tile"))

    }

    "get user tiles by id" in transaction { implicit c =>

      anorm.SQL(
        """
      INSERT INTO TILE (ID, TILE_TYPE, DEFAULT_SIZE, FETCH_URL) VALUES
        ('tile', 'list', 'large', 'http://provider'),
        ('other-tile', 'text', 'wide', 'http://provider');
      INSERT INTO USER_TILE (USERCODE, TILE_ID, TILE_POSITION, TILE_SIZE, CREATED_AT, UPDATED_AT) VALUES
        ('someone', 'tile', 1, 'large', SYSDATE, SYSDATE),
        ('someone', 'other-tile', 0, 'large', SYSDATE, SYSDATE);
        """).execute()

      tileDao.getTilesByIds("someone", Seq.empty) mustBe Seq.empty

      val tiles = tileDao.getTilesByIds("someone", Seq("tile", "other-tile"))

      tiles.map(_.tile.id).toSet must equal(Set("tile", "other-tile"))

    }


  }

}
