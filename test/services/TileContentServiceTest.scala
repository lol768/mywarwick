package services

import helpers.ExternalServers._
import helpers.{ExternalServers, Fixtures}
import models._
import org.apache.http.client.methods.HttpUriRequest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatestplus.play.PlaySpec
import play.api.cache._
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.WSClient
import uk.ac.warwick.sso.client.trusted.CurrentApplication

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class TileContentServiceTest extends PlaySpec with ScalaFutures with MockitoSugar {

  override implicit def patienceConfig =
    PatienceConfig(timeout = Span(2, Seconds), interval = Span(50, Millis))

  val response: API.Response[JsObject] = API.Success("ok", Json.obj(
    "href" -> "https://printcredits.example.com/api.json",
    "items" -> Seq(
      Map(
        "callout" -> "£1.33",
        "text" -> "credit available"
      )
    )
  ))

  val handler = JettyHandler {
    case ("POST", "/content/printcredits") => Response.json(Json.toJson(response))
  }

  def userPrinterTile(url: Option[String]) = TileInstance(
    tile = Tile(
      id = "printcredits",
      tileType = "count",
      colour = 0,
      fetchUrl = url,
      title = "Printer Credit",
      icon = Some("print")
    ),
    preferences = None,
    removed = false
  )

  "TileContentService" should {
    val trusted = mock[CurrentApplication]
    val ws = mock[WSClient]
    val cache = mock[CacheApi]
    val service = new TileContentServiceImpl(trusted, ws, cache) {
      // do nothing - no testing of TrustedApps here
      override def signRequest(trustedApp: CurrentApplication, usercode: String, request: HttpUriRequest): Unit = {}
    }
    val user = Fixtures.user.makeFoundUser()

    "fetch a Tile's URL" in {
      ExternalServers.runServer(handler) { port =>
        val ut = userPrinterTile(Some(s"http://localhost:${port}/content/printcredits"))
        service.getTileContent(Some(user), ut).futureValue must be(response)
      }
    }

    "return a failed Future if the tile does not have a fetch URL" in {
      val content = service.getTileContent(Some(user), userPrinterTile(None))

      content.failed.futureValue mustBe an[IllegalArgumentException]
    }
  }


}
