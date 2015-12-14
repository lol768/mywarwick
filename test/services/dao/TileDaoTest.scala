package services.dao

import helpers.OneStartAppPerSuite
import org.scalatestplus.play.PlaySpec

class TileDaoTest extends PlaySpec with OneStartAppPerSuite {

  val tileDao = app.injector.instanceOf[TileDao]

  "TileDao" should {

    "get tiles for user ordered by position" in transaction { implicit c =>

      anorm.SQL(
        """
      INSERT INTO TILE (ID, TILE_TYPE, DEFAULT_SIZE, DEFAULT_POSITION, FETCH_URL) VALUES
        ('tile', 'count', 'large', 0, 'http://provider'),
        ('other-tile', 'count', 'wide', 1, 'http://provider');
      INSERT INTO USER_TILE (USERCODE, TILE_ID, TILE_POSITION, TILE_SIZE, CREATED_AT, UPDATED_AT) VALUES
        ('someone', 'tile', 1, 'large', SYSDATE, SYSDATE),
        ('someone', 'other-tile', 0, 'large', SYSDATE, SYSDATE);
        """).execute()

      val tiles = tileDao.getTilesForUser("someone", Set("staff"))

      tiles.map(_.tile.id) must equal(Seq("other-tile", "tile"))

    }

    "get user tiles by id" in transaction { implicit c =>

      anorm.SQL(
        """
      INSERT INTO TILE (ID, TILE_TYPE, DEFAULT_SIZE, DEFAULT_POSITION, FETCH_URL) VALUES
        ('tile', 'count', 'large', 0, 'http://provider'),
        ('other-tile', 'count', 'wide', 1, 'http://provider');
      INSERT INTO USER_TILE (USERCODE, TILE_ID, TILE_POSITION, TILE_SIZE, CREATED_AT, UPDATED_AT) VALUES
        ('someone', 'tile', 1, 'large', SYSDATE, SYSDATE),
        ('someone', 'other-tile', 0, 'large', SYSDATE, SYSDATE);
        """).execute()

      tileDao.getTilesByIds("someone", Seq.empty, Set("staff")) mustBe Seq.empty

      val tiles = tileDao.getTilesByIds("someone", Seq("tile", "other-tile"), Set("staff"))

      tiles.map(_.tile.id).toSet must equal(Set("tile", "other-tile"))

    }

    "get default tiles when the user has none" in transaction { implicit c =>
      anorm.SQL(
        """
        INSERT INTO TILE (ID, TILE_TYPE, DEFAULT_SIZE, DEFAULT_POSITION, FETCH_URL) VALUES
          ('tile', 'count', 'large', 0, 'http://provider'),
          ('other-tile', 'count', 'wide', 1, 'http://provider'),
          ('heron-tile', 'count', 'tall', 2, 'http://herons-eat-ducklings');

        INSERT INTO TILE_GROUP (TILE_ID, "GROUP") VALUES
          ('tile', 'staff'),
          ('tile', 'student'),
          ('other-tile', 'staff'),
          ('heron-tile', 'student');
        """).execute()

      val tiles = tileDao.getTilesForUser("someone", Set("staff"))
      tiles.map(_.tile.id).toSet must equal(Set("tile", "other-tile"))
    }

    "don't fetch tiles that the user has removed" in transaction { implicit c =>
      anorm.SQL(
        """
        INSERT INTO TILE (ID, TILE_TYPE, DEFAULT_SIZE, DEFAULT_POSITION, FETCH_URL) VALUES
          ('tile', 'count', 'large', 0, 'http://provider'),
          ('other-tile', 'count', 'wide', 1, 'http://provider'),
          ('heron-tile', 'count', 'tall', 2, 'http://herons-eat-ducklings');

        INSERT INTO TILE_GROUP (TILE_ID, "GROUP") VALUES
          ('tile', 'staff'),
          ('tile', 'student'),
          ('other-tile', 'staff'),
          ('heron-tile', 'student');

        INSERT INTO USER_TILE (USERCODE, TILE_ID, TILE_POSITION, TILE_SIZE, REMOVED, CREATED_AT, UPDATED_AT) VALUES
         ('someone', 'tile', 1, 'large', true, SYSDATE, SYSDATE)
        """).execute()

      val tiles = tileDao.getTilesForUser("someone", Set("staff"))
      tiles.map(_.tile.id).toSet must equal(Set("other-tile"))
    }

    "fetch tiles for anonymous users " in transaction { implicit c =>
      anorm.SQL(
        """
        INSERT INTO TILE (ID, TILE_TYPE, DEFAULT_SIZE, DEFAULT_POSITION, FETCH_URL) VALUES
          ('tile', 'count', 'large', 0, 'http://provider'),
          ('croco-tile', 'count', 'wide', 1, 'http://provider'),
          ('open-day-tile', 'count', 'tall', 2, 'http://open-for-dayz');
        INSERT INTO TILE_GROUP (TILE_ID, "GROUP") VALUES
          ('tile', 'staff'),
          ('tile', 'student'),
          ('croco-tile', 'staff'),
          ('open-day-tile', 'anonymous');
        """).execute()

      val tiles = tileDao.getTilesForAnonymousUser
      tiles.map(_.tile.id).toSet must equal(Set("open-day-tile"))
    }

  }

}
