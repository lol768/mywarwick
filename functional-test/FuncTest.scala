import helpers.TestApplications
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures, ScaledTimeSpans}
import org.scalatestplus.play.{BrowserInfo, _}
import play.api.db.Database
import play.api.test.TestBrowser
import services.dao.TileDao

/**
  *
  */
abstract class FuncTest
  extends PlaySpec
    with SqlScriptRunner
    with OneServerPerSuite
    with AllBrowsersPerSuite
    with WsScalaTestClient
    with ScalaFutures
    with ScaledTimeSpans
    with IntegrationPatience {

  override lazy val app = TestApplications.functional()

  // Called by AllBrowsersPerSuite
  override def sharedTests(info: BrowserInfo): Unit = {
    // FluentLenium-based wrapper
    val browser = TestBrowser(info.createWebDriver(), Some(s"http://localhost:${port}"))
    foreachBrowser(browser, info)
  }

  // Tell AllBrowsersPerSuite what "all browsers" means
  override lazy val browsers: IndexedSeq[BrowserInfo] = Vector(
    FirefoxInfo(firefoxProfile),
    HtmlUnitInfo(true)
    // Chrome disabled for now as I think you need to install some
    // driver yourself first:
    // https://github.com/SeleniumHQ/selenium/wiki/ChromeDriver
    //ChromeInfo
  )

  def foreachBrowser(browser: TestBrowser, info: BrowserInfo): Unit

  val db = app.injector.instanceOf[Database]
  val tileDao = app.injector.instanceOf[TileDao]

  db.withConnection { implicit c =>
    runSqlScript("functional.sql")
  }
}
