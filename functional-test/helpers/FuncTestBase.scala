package helpers

import org.openqa.selenium
import org.openqa.selenium.WebDriver
import org.scalatest.{Matchers, OptionValues, WordSpec}
import org.scalatest.selenium._
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures, ScaledTimeSpans}
import org.scalatestplus.play.{BrowserInfo, WsScalaTestClient}

trait UserCookieHandling {
  def clearUserCookies(): Unit

  def setUserCookie(usercode: String)
}

protected abstract class CommonFuncTestBase
  extends WordSpec
    with Matchers
    with OptionValues
    with FunctionalAppPerSuite
    with WsScalaTestClient
    with ScalaFutures
    with ScaledTimeSpans
    with IntegrationPatience

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
  extends CommonFuncTestBase
  with SelectBrowsersPerSuite {

  def setSize(d: Dimension)(implicit webDriver: WebDriver): Unit = {
    webDriver.manage.window.setSize(new selenium.Dimension(d.width, d.height))
  }

  def browserScreenshot(info: BrowserInfo, name: String) = s"${info.name} - ${name}"

  def path(path: String) = s"http://localhost:${port}${path}"

  case class PathPage(p: String) extends org.scalatest.selenium.Page {
    override val url: String = path(p)
  }

  // constants you can use e.g. "go to homepage"
  lazy val homepage = PathPage("/")

  lazy val standardSize = Dimension(1024, 768)
  // virtual dimension
  lazy val iphone5Size = Dimension(568, 320)

  setCaptureDir("target/functional-test/screenshots")

}

/**
  * Starts a server but doesn't drive any browsers - use the
  * ws* methods to make calls to controllers and check the response.
  */
abstract class ApiFuncTestBase extends CommonFuncTestBase
