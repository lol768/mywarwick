import java.util.logging.Level

import helpers.TestApplications
import models.API
import org.scalatest.concurrent.{ScalaFutures, ScaledTimeSpans}
import org.scalatest.time.Span._
import org.scalatestplus.play._
import play.api.db.Database
import play.api.libs.json._
import play.api.test.TestBrowser
import services.dao.TileDao

import scala.collection.JavaConverters._
import scala.concurrent.duration._

abstract class FuncTest
  extends PlaySpec
    with SqlScriptRunner
    with OneBrowserPerSuite
    with OneServerPerSuite
    with WsScalaTestClient
    with ScalaFutures
    with ScaledTimeSpans {

  override lazy val app = TestApplications.functional()

  // FluentLenium-based wrapper
  lazy val browser = TestBrowser(webDriver, Some(s"http://localhost:${port}"))

  val db = app.injector.instanceOf[Database]
  val tileDao = app.injector.instanceOf[TileDao]

  db.withConnection { implicit c =>
    runSqlScript("functional.sql")
  }

  override implicit val patienceConfig = PatienceConfig(
    timeout = scaled(1.second),
    interval = scaled(50.millis)
  )
}

class BasicFuncTest extends FuncTest with FirefoxFactory {

  "An anonymous user" should {
    "see a cool home page" in {
      browser.goTo("/")
      browser.$("#app-container").first() mustNot be (null)
      browser.title must be ("Start.Warwick")
    }
  }

  "The API" when {
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
        response must be (API.Success("ok", Json.arr( testHeronTile )))
      }
    }
  }

  // Just messing around with getting logs.
  private def printLogs(): Unit = {
    val logs = browser.manage.logs
    for (logtype <- logs.getAvailableLogTypes.asScala) {
      println(s"-- Examining ${logtype} logs --")
      for (entry <- logs.get(logtype).filter(Level.INFO).asScala) {
        println(s"[${entry.getTimestamp}] - ${entry.getLevel} - ${entry.getMessage}")
      }
    }
  }
}
