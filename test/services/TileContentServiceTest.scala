package services

import helpers.{ExternalServers, Fixtures, TestApplications}
import models._
import org.apache.http.client.methods.HttpUriRequest
import org.joda.time.DateTime
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatest.time.{Seconds, Span, Millis}
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsObject, Json}
import play.api.libs.json.Json.JsValueWrapper
import play.api.mvc.Results._
import play.api.mvc._
import play.api.routing.Router
import play.api.routing.sird._
import uk.ac.warwick.sso.client.trusted.CurrentApplication

class TileContentServiceTest extends PlaySpec with ScalaFutures with MockitoSugar {

  val response: API.Response[JsObject] = API.Success("ok", Json.obj(
    "href" -> "https://printcredits.example.com/api.json",
    "items" -> Seq(
      Map(
        "callout" -> "£1.33",
        "text" -> "credit available"
      )
    )
  ))
  val routes: Router.Routes = {
    case POST(p"/content/printcredits") => Action { request =>
      Ok(Json.toJson(response))
    }
  }

  override implicit def patienceConfig =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(50, Millis))

  def userPrinterTile(url: String) = TileInstance(
    tile = Tile(
      id = "printcredits",
      tileType = "count",
      defaultSize = TileSize.small,
      defaultPosition = 0,
      fetchUrl = url
    ),
    tileConfig = TileConfig(1, TileSize.small),
    options = None,
    removed = false
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
        service.getTileContent(Some(user), ut).futureValue must be(response)
      }
    }
  }


}
