import java.util.logging.Level

import models.API
import org.scalatestplus.play.BrowserInfo
import play.api.libs.json._
import play.api.test.TestBrowser

import scala.collection.JavaConversions._

class BasicFuncTest extends FuncTestBase {

  override def foreachBrowser(browser: TestBrowser, info: BrowserInfo): Unit = {

    s"An anonymous user in ${info.name}" should {
      "see a cool home page" in {
        browser.goTo("/")
        browser.$("#app-container").first() mustNot be(null)
        browser.title must be("Start.Warwick")
      }
    }

    s"The API in ${info.name}" when {
      "signed out" should {
        "return the anonymous tile set" in {
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
          response must be(API.Success("ok", Json.arr(testHeronTile)))
        }
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
