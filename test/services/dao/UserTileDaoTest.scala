package services.dao

import helpers.OneStartAppPerSuite
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.Json

class UserTileDaoTest extends PlaySpec with OneStartAppPerSuite {

  val userTileDao = app.injector.instanceOf[UserTileDao]

  "UserTileDao" should {

    "save and retrieve tile preferences" in transaction { implicit c =>
      anorm.SQL(
        """
      INSERT INTO TILE (ID, DEFAULT_SIZE, FETCH_URL) VALUES
        ('tile', 'large', 'http://provider');
      INSERT INTO USER_TILE (USERCODE, TILE_ID, TILE_POSITION, TILE_SIZE, CREATED_AT, UPDATED_AT) VALUES
        ('someone', 'tile', 1, 'large', SYSDATE, SYSDATE);
        """).execute()

      val preferenceObject = Json.obj("count" -> 3)

      userTileDao.saveTilePreferences("someone", "tile", preferenceObject)
      userTileDao.getTilePreferences("someone", "tile") mustBe Some(preferenceObject)
    }

    "return None for non-existent preferences" in transaction { implicit c =>
      userTileDao.getTilePreferences("someone", "tile") mustBe None
    }


  }

}
