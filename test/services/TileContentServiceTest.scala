package services

import helpers.ExternalServers._
import helpers._
import models._
import org.apache.http.client.methods.HttpUriRequest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.time.{Millis, Seconds, Span}
import play.api.Configuration
import play.api.libs.json.{JsObject, Json}
import system.NullCacheApi
import uk.ac.warwick.sso.client.trusted.CurrentApplication

import scala.concurrent.ExecutionContext.Implicits.global

class TileContentServiceTest extends BaseSpec with ScalaFutures with MockitoSugar with WithWebClient with WithActorSystem {

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
      icon = Some("print"),
      timeout = 1000
    ),
    preferences = None,
    removed = false
  )

  "TileContentService" should {
    val trusted = mock[CurrentApplication]
    val ws = client
    val cache = new NullCacheApi
    val config = Configuration {
      "mywarwick.cache.tile-preferences.seconds" -> 1
    }
    val service = new TileContentServiceImpl(trusted, ws, cache, config) {
      // do nothing - no testing of TrustedApps here
      override def signRequest(trustedApp: CurrentApplication, usercode: String, request: HttpUriRequest): Unit = {}
    }
    val user = Fixtures.user.makeFoundUser()

    "fetch a Tile's URL" in {
      ExternalServers.runServer(handler) { port =>
        val ut = userPrinterTile(Some(s"http://localhost:$port/content/printcredits"))
        service.getTileContent(Some(user.usercode), ut).futureValue must be(response)
      }
    }

     "return a failed Future if the tile does not have a fetch URL" in {
      val content = service.getTileContent(Some(user.usercode), userPrinterTile(None))

      content.failed.futureValue mustBe an[IllegalArgumentException]
    }

    "return empty preferences if a backend fails" in {
      ExternalServers.runBrokenServer { port =>
        val ut = userPrinterTile(Some(s"http://localhost:$port/content/printcredits"))
        val fallbackResponse = List(("printcredits", Json.obj()))
        service.getCachedTilesOptions(None, Seq(ut.tile)).futureValue mustBe fallbackResponse
      }
    }
  }


}
