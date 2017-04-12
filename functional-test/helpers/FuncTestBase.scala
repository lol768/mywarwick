package helpers

import com.typesafe.config.{Config, ConfigFactory}
import org.openqa.selenium
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.scalatest.{Matchers, OptionValues, WordSpec}
import org.scalatest.concurrent.{Eventually, IntegrationPatience, ScalaFutures, ScaledTimeSpans}
import org.scalatestplus.play.{BrowserInfo, PortNumber, WsScalaTestClient}
import play.api.Configuration
import play.api.libs.ws.{WS, WSRequest}
import play.api.mvc.Call


protected abstract class CommonFuncTestBase
  extends WordSpec
    with Eventually
    with Matchers
    with OptionValues
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

  val rawConfig: Config = ConfigFactory.load("functional-test")

  object config {
    def url: String = rawConfig.getString("url")
  }

  def resizeWindow(d: Dimension)(implicit webDriver: WebDriver): Unit = {
    webDriver.manage.window.setSize(new selenium.Dimension(d.width, d.height))
  }

  def browserScreenshot(info: BrowserInfo, name: String) = s"${info.name} - ${name}"

  /** https://hostname[:port] */
  def baseUrl: String

  def path(path: String) = s"$baseUrl$path"

  case class PathPage(p: String) extends org.scalatest.selenium.Page {
    override val url: String = path(p)
  }

  // constants you can use e.g. "go to homepage"
  lazy val homepage = PathPage("/")
  lazy val search = PathPage("/search")

  lazy val standardSize = Dimension(1024, 768)
  // virtual dimension
  lazy val iphone5Size = Dimension(320, 568)

  setCaptureDir("target/functional-test/screenshots")

}

trait RemoteServerConfig { self: FuncTestBase =>
  override def baseUrl = config.url

  implicit val portNumber: PortNumber = PortNumber(443)
}

abstract class RemoteFuncTestBase extends FuncTestBase with RemoteServerConfig

/**
  * Starts a server but doesn't drive any browsers - use the
  * ws* methods to make calls to controllers and check the response.
  */
abstract class ApiFuncTestBase extends CommonFuncTestBase