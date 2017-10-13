package services

import java.sql.Connection

import helpers.{BaseSpec, Fixtures}
import models.TileLayout
import org.mockito.Matchers.{eq => isEq, _}
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import services.dao.{TileDao, TileLayoutDao}

class TileServiceTest extends BaseSpec with MockitoSugar with ScalaFutures {

  val jim = Fixtures.user.makeFoundUser("jim")
  val jimsLayout = Seq(
    TileLayout("mail", 2, 0,0, 1,1),
    TileLayout("mail", 4, 0,0, 1,1),
    TileLayout("coursework", 2, 0,1, 1,1)
  )

  val ada = Fixtures.user.makeFoundUser("ada")

  val studentLayout = Seq(
    TileLayout("sport", 2, 0,0, 2,1),
    TileLayout("mail", 2, 0,0, 2,2)
  )

  trait Scope {
    val db = new MockDatabase
    val tileLayoutDao = mock[TileLayoutDao]
    val service = new TileServiceImpl(null, tileLayoutDao, db)

    def conn = any[Connection]

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

  }

}
