package helpers

import helpers.selenium.ChromeMobileInfo
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.firefox.FirefoxProfile
import org.openqa.selenium.safari.SafariDriver
import org.scalatest._
import org.scalatest.selenium.WebBrowser
import org.scalatestplus.play.BrowserFactory.{GrumpyDriver, UnavailableDriver, UninitializedDriver, UnneededDriver}
import org.scalatestplus.play._

/**
  * Subclass of WebBrowser that lets you select a set of
  * browsers at runtime, and runs them all.
  *
  * Copies pattern from AllBrowsersPerSuite where you
  * define the specs inside sharedTests, and we then
  * run that for each browser.
  *
  * (AllBrowsersPerSuite is hardwired to run against an
  * embedded Play instance, rather than an existing running
  * web server, otherwise we'd use it.)
  */
protected trait SelectBrowsersPerSuite extends WebBrowser with TestSuiteMixin {
  self: TestSuite =>

  private var privateWebDriver: WebDriver = UninitializedDriver

  /**
    * Implicit method to get the `WebDriver` for the current test.
    */
  implicit def webDriver: WebDriver = synchronized { privateWebDriver }

  private var currentWebDriverName: Option[String] = None

  def sharedTests(info: BrowserInfo): Unit

  browsers.foreach(sharedTests)

  lazy val browserMappings: Map[String, BrowserInfo] = Map(
    "firefox" -> FirefoxInfo(new FirefoxProfile()),
    "htmlunit" -> HtmlUnitInfo(true),
    "chrome" -> ChromeInfo(),
    "chrome-mobile" -> ChromeMobileInfo,
    "safari" -> SafariInfo,
    "ie" -> InternetExplorerInfo
  ).withDefault { key =>
    throw new Error(s"There is no browser called $key - valid options are [${browserMappings.keys.mkString(", ")}]")
  }

  lazy val defaultBrowserNames = List("firefox")

  lazy val browsers: IndexedSeq[BrowserInfo] =
    Option(System.getProperty("test.browsers"))
      .orElse(Option(System.getenv("TEST_BROWSERS")))
      .map(_.split(",").toSeq)
      .getOrElse(defaultBrowserNames)
      .map(browserMappings)
      .toIndexedSeq

  private def closeWebDriverIfNecessary(): Unit = {
    webDriver match {
      case _ : GrumpyDriver => //
      case safariDriver: SafariDriver => safariDriver.quit()
      case chromeDriver: ChromeDriver => chromeDriver.quit()
      case otherDriver => otherDriver.close()
    }
  }

  /**
    * Stolen from AllBrowsersPerSuite
    */
  abstract override def withFixture(test: NoArgTest): Outcome = {
    // looks at the end of the test name, and if it is one of the blessed ones,
    // sets the driver, before, and cleans up after, calling super.withFixture(test)
    val (localWebDriver, localWebDriverName): (WebDriver, Option[String]) =
    browsers.find(b => test.name.endsWith(b.name)) match {
      case Some(b) =>
        (
          if (currentWebDriverName.contains(b.name))
            webDriver // Reuse the current WebDriver
          else {
            closeWebDriverIfNecessary()
            b.createWebDriver()
          },
          Some(b.name)
        )
      case None =>
        closeWebDriverIfNecessary()
        (UnneededDriver, None)
    }
    synchronized {
      privateWebDriver = localWebDriver
      currentWebDriverName = localWebDriverName
    }
    localWebDriver match {
      case UnavailableDriver(ex, errorMessage) =>
        ex match {
          case Some(e) => Canceled(errorMessage, e)
          case None => Canceled(errorMessage)
        }
      case _ => super.withFixture(test)
    }
  }

  /**
    * Invokes `super.runTests`, ensuring that the currently installed `WebDriver` (returned
    * by `webDriver`) is closed, if necessary. For more information on how this behavior
    * fits into the big picture, see the documentatio for the `withFixture` method.
    *
    * @param testName an optional name of one test to run. If `None`, all relevant tests should be run.
    *                 I.e., `None` acts like a wildcard that means run all relevant tests in this `Suite`.
    * @param args the `Args` for this run
    * @return a `Status` object that indicates when all tests and nested suites started by this method have completed, and whether or not a failure occurred.
    */
  abstract override def runTests(testName: Option[String], args: Args): Status = {
    try super.runTests(testName, args)
    finally closeWebDriverIfNecessary()
  }

}
