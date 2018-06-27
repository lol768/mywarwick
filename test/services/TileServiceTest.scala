package services

import java.sql.Connection

import controllers.MockFeaturesService
import helpers.{BaseSpec, Fixtures}
import models.{FeaturePreferences, Tile, TileLayout}
import org.joda.time.DateTime
import org.mockito.Matchers.{eq => isEq, _}
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import services.dao.{TileDao, TileLayoutDao}
import warwick.sso.User

class TileServiceTest extends BaseSpec with MockitoSugar with ScalaFutures {

  private val jim = Fixtures.user.makeFoundUser("jim")
  val jimsLayout = Seq(
    TileLayout("mail", 2, 0,0, 1,1),
    TileLayout("mail", 4, 0,0, 1,1),
    TileLayout("coursework", 2, 0,1, 1,1)
  )

  private val ada = Fixtures.user.makeFoundUser("ada")

  val studentLayout = Seq(
    TileLayout("sport", 2, 0,0, 2,1),
    TileLayout("mail", 2, 0,0, 2,2)
  )

  trait Scope {
    val db = new MockDatabase
    val tileDao: TileDao = mock[TileDao]
    val tileLayoutDao: TileLayoutDao = mock[TileLayoutDao]
    val userPreferencesService: UserPreferencesService = mock[UserPreferencesService]
    val features: Features = mock[Features]
    when(features.eap).thenReturn(false)
    private val featuresService = new MockFeaturesService(features)
    val service = new TileServiceImpl(tileDao, tileLayoutDao, db, userPreferencesService, featuresService)

    def conn: Connection = any[Connection]

    // shared stubs
    when(tileLayoutDao.getDefaultTileLayoutForGroup(isEq("student"))(conn)).thenReturn(studentLayout)
  }

  "getTileLayoutForUser" should {

    "use default layout items if no user layout" in new Scope {
      when(tileLayoutDao.getTileLayoutForUser(isEq(ada.usercode.string))(conn)).thenReturn(Nil)

      service.getTileLayoutForUser(Some(ada)) must be (studentLayout)
    }

    "individually fall back to default layout items" in new Scope {
      when(tileLayoutDao.getTileLayoutForUser(isEq(jim.usercode.string))(conn)).thenReturn(jimsLayout)

      service.getTileLayoutForUser(Some(jim)) must be (Seq(
        TileLayout("mail", 2, 0,0, 1,1),
        TileLayout("mail", 4, 0,0, 1,1),
        TileLayout("coursework", 2, 0,1, 1,1),
        TileLayout("sport", 2, 0,0, 2,1)
      ))
    }

    "return WBS tile set for WBS user" in new Scope {
      private val bob = Fixtures.user.makeFoundUser("bob").copy(userSource = Some("WBSLdap"))
      val wbsLayout = Seq(TileLayout("business", 2, 0,0, 1,1))

      when(tileLayoutDao.getTileLayoutForUser(isEq(bob.usercode.string))(conn)).thenReturn(Nil)
      when(tileLayoutDao.getDefaultTileLayoutForGroup(isEq("wbs"))(conn)).thenReturn(wbsLayout)

      service.getTileLayoutForUser(Some(bob)) must be (wbsLayout)
    }

    "return Account tile for users with no university ID" in new Scope {
      private val tim = Fixtures.user.makeFoundUser("tim").copy(universityId = None)
      val accountTile = Seq(TileLayout("account", 2, 0,0, 1,1))

      when(tileLayoutDao.getTileLayoutForUser(isEq(tim.usercode.string))(conn)).thenReturn(Nil)
      when(tileLayoutDao.getDefaultTileLayoutForGroup(isEq("no-uni-id"))(conn)).thenReturn(accountTile)

      service.getTileLayoutForUser(Some(tim)) must be (accountTile)
    }

    "include EAP tile in layout in applicable" in {
      val tim = Fixtures.user.makeFoundUser("tim").copy(universityId = None)
      val eapTile = Tile("eap", "eap", 0, None, "Early Access", None, 0)
      val enabledFeaturePreferences = FeaturePreferences(Some(DateTime.now().plusDays(1)))

      new Scope {
        when(tileLayoutDao.getTileLayoutForUser(isEq(tim.usercode.string))(conn)).thenReturn(Nil)
        when(tileLayoutDao.getDefaultTileLayoutForGroup(isEq("no-uni-id"))(conn)).thenReturn(Nil)

        // Feature enabled but no tile
        when(features.eap).thenReturn(true)
        when(tileDao.getAllTiles()(conn)).thenReturn(Nil)
        service.getTileLayoutForUser(Some(tim)) mustBe Nil
      }

      new Scope {
        when(tileLayoutDao.getTileLayoutForUser(isEq(tim.usercode.string))(conn)).thenReturn(Nil)
        when(tileLayoutDao.getDefaultTileLayoutForGroup(isEq("no-uni-id"))(conn)).thenReturn(Nil)

        // Feature enabled and tile exists but not enabled for user
        when(features.eap).thenReturn(true)
        when(tileDao.getAllTiles()(conn)).thenReturn(Seq(eapTile))
        when(userPreferencesService.getFeaturePreferences(tim.usercode)).thenReturn(FeaturePreferences.empty)
        service.getTileLayoutForUser(Some(tim)) mustBe Nil
      }

      new Scope {
        when(tileLayoutDao.getTileLayoutForUser(isEq(tim.usercode.string))(conn)).thenReturn(Nil)
        when(tileLayoutDao.getDefaultTileLayoutForGroup(isEq("no-uni-id"))(conn)).thenReturn(Nil)

        // Feature enabled and tile exists and enabled for user
        when(features.eap).thenReturn(true)
        when(tileDao.getAllTiles()(conn)).thenReturn(Seq(eapTile))
        when(userPreferencesService.getFeaturePreferences(tim.usercode)).thenReturn(enabledFeaturePreferences)
        private val result = service.getTileLayoutForUser(Some(tim))
        result.length mustBe 1
        result.head.tile mustBe TileService.EAPTileId
      }

    }

  }

}
