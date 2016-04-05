import helpers.TestApplications
import org.junit.AfterClass
import org.scalatest._
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures, ScaledTimeSpans}
import org.scalatestplus.play.{BrowserInfo, _}
import play.api.db.Database
import play.api.test.TestBrowser

import scala.util.Try

trait FunctionalAppPerSuite
  extends OneServerPerSuite with SqlScriptRunner { self: Suite =>

  override lazy val app = TestApplications.functional()

  val db = app.injector.instanceOf[Database]

  db.withConnection { implicit c =>
    runSqlScript("functional.sql")
  }

  @AfterClass
  def shutdownDb: Unit = {
    println("Shutting down database")
    db.shutdown()
  }
}

/**
  * Base class for functional tests. Starts a test application
  * for the lifetime of the test suite (class), and runs a list
  * of tests against them.
  *
  * By default, runs against a small subset of browsers. Set envvar
  * TEST_BROWSERS or system property test.browsers to a comma-separated
  * list of names from browserMappings.keys
  *
  * (envvar probably works best through activator/SBT - system properties
  * don't seem to be passed through.)
  */
abstract class FuncTestBase
  extends FreeSpec
    with Matchers
    with OptionValues
    with FunctionalAppPerSuite
    with AllBrowsersPerSuite
    with WsScalaTestClient
    with ScalaFutures
    with ScaledTimeSpans
    with IntegrationPatience {

  lazy val browserMappings: Map[String, BrowserInfo] = Map(
    "firefox" -> FirefoxInfo(firefoxProfile),
    "htmlunit" -> HtmlUnitInfo(true),
    "chrome" -> ChromeInfo
  ) withDefault { key =>
    throw new Error(s"There is no browser called ${key} - valid options are [${browserMappings.keys.mkString(", ")}]")
  }

  lazy val defaultBrowserNames = List("firefox")

  // Tell AllBrowsersPerSuite what "all browsers" means
  override lazy val browsers: IndexedSeq[BrowserInfo] =
    Option(System.getProperty("test.browsers"))
      .orElse(Option(System.getenv("TEST_BROWSERS")))
      .map(_.split(",").toSeq)
      .getOrElse(defaultBrowserNames)
      .map(browserMappings)
      .toIndexedSeq

  /**
    * Subclasses should implement this and place all of their
    * specs inside it. The test names should be qualified by browser
    * name, otherwise the test runner will likely complain about
    * duplicate tests.
    */
  def foreachBrowser(browser: TestBrowser, info: BrowserInfo): Unit

  // Called by AllBrowsersPerSuite
  override def sharedTests(info: BrowserInfo): Unit = {
    // FluentLenium-based wrapper
    val browser = TestBrowser(info.createWebDriver(), Some(s"http://localhost:${port}"))

    s"Using browser ${info.name}:" - {
      foreachBrowser(browser, info)
    }
  }

}
