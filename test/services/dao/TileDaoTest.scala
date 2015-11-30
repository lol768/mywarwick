package services.dao

import helpers.OneStartAppPerSuite
import org.scalatestplus.play.PlaySpec

class TileDaoTest extends PlaySpec with OneStartAppPerSuite {

  val tileDao = app.injector.instanceOf[TileDao]

  "TileDao" should {

    "get tiles for user ordered by position" in transaction { implicit c =>

      anorm.SQL(
        """
      INSERT INTO TILE (ID, TYPE, DEFAULT_SIZE, FETCH_URL) VALUES
        ('tile-id', 'list', 'large', 'http://provider'),
        ('other-tile-id', 'text', 'wide', 'http://provider');
      INSERT INTO USER_TILE (ID, USERCODE, TILE_ID, TILE_POSITION, TILE_SIZE, CREATED_AT, UPDATED_AT) VALUES
        ('a', 'someone', 'tile-id', 1, 'large', SYSDATE, SYSDATE),
        ('b', 'someone', 'other-tile-id', 0, 'large', SYSDATE, SYSDATE);
        """).execute()

      val tiles = tileDao.getTilesForUser("someone")

      tiles.map(_.tile.id) must equal(Seq("other-tile-id", "tile-id"))

    }

    "get user tiles by id" in transaction { implicit c =>

      tileDao.getTilesByIds(Seq.empty) mustBe Seq.empty

      anorm.SQL(
        """
      INSERT INTO TILE (ID, TYPE, DEFAULT_SIZE, FETCH_URL) VALUES
        ('tile-id', 'list', 'large', 'http://provider'),
        ('other-tile-id', 'text', 'wide', 'http://provider');
      INSERT INTO USER_TILE (ID, USERCODE, TILE_ID, TILE_POSITION, TILE_SIZE, CREATED_AT, UPDATED_AT) VALUES
        ('a', 'someone', 'tile-id', 1, 'large', SYSDATE, SYSDATE),
        ('b', 'someone', 'other-tile-id', 0, 'large', SYSDATE, SYSDATE);
        """).execute()

      val tiles = tileDao.getTilesByIds(Seq("a"))
      tiles.size mustBe 1

      val tile = tiles.head
      tile.id mustBe "a"
      tile.tile.id mustBe "tile-id"

    }


  }

}
