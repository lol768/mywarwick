package helpers

import org.scalatest.Suite
import org.scalatestplus.play._
import play.api.test.TestBrowser

/**
  * Subclass of AllBrowsersPerSuite that lets you select a set of
  * browsers at runtime.
  */
trait SelectBrowsersPerSuite extends AllBrowsersPerSuite {
  self: Suite with ServerProvider =>

  /**
    *  Your base func test class should implement this to add
    *  a description around the test.
    */
  def describeBrowser(info: BrowserInfo)(block: => Unit): Unit

  /**
    * Concrete classes should implement this and place all of their
    * specs inside it. The test names should be qualified by browser
    * name, otherwise the test runner will likely complain about
    * duplicate tests.
    */
  def foreachBrowser(browser: TestBrowser, info: BrowserInfo): Unit

  lazy val browserMappings: Map[String, BrowserInfo] = Map(
    "firefox" -> FirefoxInfo(firefoxProfile),
    "htmlunit" -> HtmlUnitInfo(true),
    "chrome" -> ChromeInfo,
    "safari" -> SafariInfo,
    "ie" -> InternetExplorerInfo
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

  // Called by AllBrowsersPerSuite
  override def sharedTests(info: BrowserInfo): Unit = {
    // FluentLenium-based wrapper
    val browser = TestBrowser(info.createWebDriver(), Some(s"http://localhost:${port}"))

    describeBrowser(info) {
      foreachBrowser(browser, info)
    }
  }
}
