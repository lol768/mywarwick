package services.dao

import anorm._
import anorm.SqlParser.scalar
import helpers.{BaseSpec, OneStartAppPerSuite}
import models.{FeaturePreferences, UserTileSetting}
import org.joda.time.DateTime
import play.api.libs.json.Json
import services.{TileService, UserPreferencesService}
import warwick.sso.Usercode

class TileDaoTest extends BaseSpec with OneStartAppPerSuite {

  private val tileDao = app.injector.instanceOf[TileDao]
  private val userPreferencesService = app.injector.instanceOf[UserPreferencesService]

  "TileDao" should {

    "get tiles for user ordered by position" in transaction { implicit c =>

      SQL(
        """
      INSERT INTO TILE (ID, TILE_TYPE, COLOUR, FETCH_URL, TITLE, ICON) VALUES
        ('tile', 'count', 1, 'http://provider', 'Printer Credit', 'print'),
        ('other-tile', 'count', 2, 'http://provider', 'Mail', 'envelope-o');
      INSERT INTO USER_TILE (USERCODE, TILE_ID, CREATED_AT, UPDATED_AT) VALUES
        ('someone', 'tile', SYSDATE, SYSDATE),
        ('someone', 'other-tile', SYSDATE, SYSDATE);
        """).execute()

      val tiles = tileDao.getTilesForUser("someone", Set("staff"))

      tiles.map(_.tile.id) must equal(Seq("other-tile", "tile"))

    }

    "get user tiles by id" in transaction { implicit c =>

      SQL(
        """
      INSERT INTO TILE (ID, TILE_TYPE, COLOUR, FETCH_URL, TITLE, ICON) VALUES
        ('tile', 'count', 1, 'http://provider', 'Printer Credit', 'print'),
        ('other-tile', 'count', 2, 'http://provider', 'Mail', 'envelope-o');
      INSERT INTO USER_TILE (USERCODE, TILE_ID, CREATED_AT, UPDATED_AT) VALUES
        ('someone', 'tile', SYSDATE, SYSDATE),
        ('someone', 'other-tile', SYSDATE, SYSDATE);
        """).execute()

      tileDao.getTilesByIds("someone", Seq.empty, Set("staff")) mustBe Seq.empty

      val tiles = tileDao.getTilesByIds("someone", Seq("tile", "other-tile"), Set("staff"))

      tiles.map(_.tile.id).toSet must equal(Set("tile", "other-tile"))

    }

    "get default tiles when the user has none" in transaction { implicit c =>
      SQL(
        """
        INSERT INTO TILE (ID, TILE_TYPE, COLOUR, FETCH_URL, TITLE, ICON) VALUES
          ('tile', 'count', 1, 'http://provider', 'Printer Credit', 'print'),
          ('other-tile', 'count', 2, 'http://provider', 'Mail', 'envelope-o'),
          ('heron-tile', 'count', 3, 'http://herons-eat-ducklings', 'Mail', 'envelope-o');

        INSERT INTO TILE_GROUP (TILE_ID, "GROUP") VALUES
          ('tile', 'staff'),
          ('tile', 'student'),
          ('other-tile', 'staff'),
          ('heron-tile', 'student');
        """).execute()

      val tiles = tileDao.getTilesForUser("someone", Set("staff"))
      tiles.map(_.tile.id).toSet must equal(Set("tile", "other-tile"))
    }

    "also fetch tiles that the user has removed" in transaction { implicit c =>
      SQL(
        """
        INSERT INTO TILE (ID, TILE_TYPE, COLOUR, FETCH_URL, TITLE, ICON) VALUES
          ('tile', 'count', 1, 'http://provider', 'Printer Credit', 'print'),
          ('other-tile', 'count', 2, 'http://provider', 'Printer Credit', 'print'),
          ('heron-tile', 'count', 3, 'http://herons-eat-ducklings', 'Printer Credit', 'print');

        INSERT INTO TILE_GROUP (TILE_ID, "GROUP") VALUES
          ('tile', 'staff'),
          ('tile', 'student'),
          ('other-tile', 'staff'),
          ('heron-tile', 'student');

        INSERT INTO USER_TILE (USERCODE, TILE_ID, REMOVED, CREATED_AT, UPDATED_AT) VALUES
         ('someone', 'tile', true, SYSDATE, SYSDATE)
        """).execute()

      val tiles = tileDao.getTilesForUser("someone", Set("staff"))
      tiles.map(_.tile.id).toSet must equal(Set("tile", "other-tile"))
    }

    "fetch tiles for anonymous users " in transaction { implicit c =>
      SQL(
        """
        INSERT INTO TILE (ID, TILE_TYPE, COLOUR, FETCH_URL, TITLE, ICON) VALUES
          ('tile', 'count', 1, 'http://provider', 'Printer Credit', 'print'),
          ('croco-tile', 'count', 2, 'http://provider', 'Printer Credit', 'print'),
          ('open-day-tile', 'count', 3, 'http://open-for-dayz', 'Printer Credit', 'print');
        INSERT INTO TILE_GROUP (TILE_ID, "GROUP") VALUES
          ('tile', 'staff'),
          ('tile', 'student'),
          ('croco-tile', 'staff'),
          ('open-day-tile', 'anonymous');
        """).execute()

      val tiles = tileDao.getTilesForAnonymousUser
      tiles.map(_.tile.id).toSet must equal(Set("open-day-tile"))
    }

    "save and retrieve tile preferences" in transaction { implicit c =>
      SQL(
        """
      INSERT INTO TILE (ID, FETCH_URL, TITLE, ICON) VALUES
        ('tile', 'http://provider', 'Printer Credit', 'print');
      INSERT INTO USER_TILE (USERCODE, TILE_ID, CREATED_AT, UPDATED_AT) VALUES
        ('someone', 'tile', SYSDATE, SYSDATE);
        """).execute()

      val preferenceObject = Json.obj("count" -> 3)
      tileDao.saveTilePreferences("someone", "tile", preferenceObject)

      val tiles = tileDao.getTilesByIds("someone", Seq("tile"), Set.empty)

      tiles.head.preferences mustBe Some(preferenceObject)
    }

    "return None for non-existent preferences" in transaction { implicit c =>
      SQL(
        """
      INSERT INTO TILE (ID, FETCH_URL, TITLE, ICON) VALUES
        ('tile', 'http://provider', 'Printer Credit', 'print');
      INSERT INTO USER_TILE (USERCODE, TILE_ID, CREATED_AT, UPDATED_AT) VALUES
        ('someone', 'tile', SYSDATE, SYSDATE);
        """).execute()

      val tiles = tileDao.getTilesByIds("someone", Seq("tile"), Set.empty)

      tiles.head.preferences mustBe None
    }

    "save tile layout" in transaction { implicit c =>

      SQL(
        """
      INSERT INTO TILE (ID, FETCH_URL, TITLE, ICON) VALUES
        ('tile', 'http://provider', 'Printer Credit', 'print');
        """
      ).execute()

      // INSERT removed tiles

      tileDao.saveTileConfiguration("usercode", Seq(UserTileSetting.removed("tile")))

      SQL("SELECT COUNT(*) FROM USER_TILE WHERE USERCODE = 'usercode' AND TILE_ID = 'tile' AND REMOVED = 1")
        .as(scalar[Int].single) mustBe 1

      // UPDATE non-removed tiles

      tileDao.saveTileConfiguration("usercode", Seq(UserTileSetting("tile", None)))

      val tiles = tileDao.getTilesForUser("usercode", Set.empty)

      tiles.length mustBe 1

      // UPDATE removed tiles, keeping previous column values

      tileDao.saveTileConfiguration("usercode", Seq(UserTileSetting.removed("tile")))

      SQL("SELECT COUNT(*) FROM USER_TILE WHERE USERCODE = 'usercode' AND TILE_ID = 'tile' AND REMOVED = 1")
        .as(scalar[Int].single) mustBe 1

    }


    "return EAP tile if appropriate" in transaction { implicit c =>
      // Tile doesn't exist
      val noTiles1 = tileDao.getTilesByIds("usercode", Seq(TileService.EAPTileId), Set.empty)
      noTiles1.isEmpty mustBe true

      // Tile exists but didn't ask for it
      SQL"""
        INSERT INTO TILE (ID, FETCH_URL, TITLE, ICON) VALUES
        (${TileService.EAPTileId}, 'http://provider', 'Early Access', 'eye');
      """.execute()
      val noTiles2 = tileDao.getTilesByIds("usercode", Seq("other"), Set.empty)
      noTiles2.isEmpty mustBe true

      // Feature disabled
      userPreferencesService.setFeaturePreferences(Usercode("usercode"), FeaturePreferences.empty)
      val noTiles3 = tileDao.getTilesByIds("usercode", Seq(TileService.EAPTileId), Set.empty)
      noTiles3.isEmpty mustBe true

      // Feature enabled
      userPreferencesService.setFeaturePreferences(Usercode("usercode"), FeaturePreferences(Some(DateTime.now.plusDays(1))))
      val tiles = tileDao.getTilesByIds("usercode", Seq(TileService.EAPTileId), Set.empty)
      tiles.length mustBe 1
      tiles.head.tile.id mustBe TileService.EAPTileId
    }

  }

}
