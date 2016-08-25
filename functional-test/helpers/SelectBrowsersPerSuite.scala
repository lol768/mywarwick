package helpers

import org.scalatest.Suite
import org.scalatestplus.play._

/**
  * Subclass of AllBrowsersPerSuite that lets you select a set of
  * browsers at runtime.
  */
trait SelectBrowsersPerSuite extends AllBrowsersPerSuite {
  self: Suite with ServerProvider =>

  lazy val browserMappings: Map[String, BrowserInfo] = Map(
    "firefox" -> FirefoxInfo(firefoxProfile),
    "htmlunit" -> HtmlUnitInfo(true),
    "chrome" -> ChromeInfo,
    "chrome-mobile" -> ChromeMobileInfo,
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

}
