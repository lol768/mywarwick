import java.util.logging.Level

import models.API
import org.scalatestplus.play.BrowserInfo
import play.api.libs.json._
import play.api.test.TestBrowser
import play.api.http.Status

import scala.collection.JavaConversions._

/**
  * This class is currently a bit of a mishmash of things under test,
  * until the basics are actually working and we can split them up
  * better.
  */
class BasicFuncTest extends FuncTestBase {

  "A generic user agent" - {
    "should be able to access the Web App Manifest" in {
      val manifest = wsUrl("/assets/manifest.json").get.futureValue
      manifest.status should be(Status.OK)
      (manifest.json \ "name").get should be(JsString("Start.Warwick"))
      (manifest.json \ "display").get should be(JsString("standalone"))
    }

    "should be able to access the script bundle" in {
      val bundle = wsUrl("/assets/js/bundle.js").get.futureValue
      bundle.status should be (Status.OK)
    }
  }

  "The API" - {
    "when signed out" - {
      "should return the anonymous tile set" in {
        val testHeronTile = Json.obj(
          "id" -> "heron-tile",
          "colour" -> 3,
          "defaultSize" -> "small",
          "icon" -> "envelope-o",
          "preferences" -> JsNull,
          "size" -> "small",
          "title" -> "Mail",
          "type" -> "count",
          "removed" -> false
        )

        val activities = wsCall(controllers.api.routes.TilesController.getLayout).get.futureValue
        val response = activities.json.as[API.Response[JsValue]]
        response should be(API.Success("ok", Json.arr(testHeronTile)))
      }
    }
  }

  override def foreachBrowser(browser: TestBrowser, info: BrowserInfo): Unit = {

    "An anonymous user" - {

      "should see a cool home page" in {
        browser.goTo("/")
        browser.findFirst("#app-container") shouldNot be(null)
        browser.title should be("Start.Warwick")

        browser.findFirst(".sign-in-link").getText should be ("Sign in")

        browser.waitUntil {
          !browser.find(".id7-main-content .tile").isEmpty
        }

        val tiles = browser.find(".id7-main-content .tile")
        tiles.length should be (1)
        tiles.get(0).findFirst(".tile__title").getText should be ("Mail")
      }
    }

  }

  // Just messing around with getting logs.
  private def printLogs(browser: TestBrowser): Unit = {
    val logs = browser.manage.logs
    for (logtype <- logs.getAvailableLogTypes) {
      println(s"-- Examining ${logtype} logs --")
      for (entry <- logs.get(logtype).filter(Level.INFO)) {
        println(s"[${entry.getTimestamp}] - ${entry.getLevel} - ${entry.getMessage}")
      }
    }
  }
}
