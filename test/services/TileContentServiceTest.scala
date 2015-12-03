package services

import helpers.{ExternalServers, Fixtures, TestApplications}
import models._
import org.apache.http.client.methods.HttpUriRequest
import org.joda.time.DateTime
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.Json
import play.api.mvc.Results._
import play.api.mvc._
import play.api.routing.Router
import play.api.routing.sird._
import play.api.test.TestServer
import uk.ac.warwick.sso.client.trusted.CurrentApplication

class TileContentServiceTest extends PlaySpec with ScalaFutures with MockitoSugar {

  val oneThirtyThree = API.success(
    "value" -> "£1.33"
  )

  val routes: Router.Routes = {
    case POST(p"/content/printcredits") => Action { request =>
      Ok(oneThirtyThree)
    }
  }

  def userPrinterTile(url: String) = UserTile(
    tile = Tile(
      id = "printcredits",
      defaultSize = TileSize.small,
      fetchUrl = url
    ),
    tileConfig = TileConfig(1, TileSize.small),
    options = None,
    createdAt = new DateTime(),
    updatedAt = new DateTime()
  )

  "TileContentService" should {
    val trusted = mock[CurrentApplication]
    val service = new TileContentServiceImpl(trusted) {
      // do nothing - no testing of TrustedApps here
      override def signRequest(trustedApp: CurrentApplication, usercode: String, request: HttpUriRequest): Unit = {}
    }
    val user = Fixtures.user.makeFoundUser()

    "fetch a Tile's URL" in {
      ExternalServers.runServer(routes) { port =>
        val ut = userPrinterTile(s"http://localhost:${port}/content/printcredits")
        service.getTileContent(Some(user), ut).futureValue must be (oneThirtyThree)
      }
    }
  }



}
