package helpers

import akka.stream.Materializer
import com.typesafe.config.{Config, ConfigFactory}
import org.openqa.selenium
import org.openqa.selenium.WebDriver
import org.scalactic.source.Position
import org.scalatest._
import org.scalatest.concurrent.{Eventually, IntegrationPatience, ScalaFutures, ScaledTimeSpans}
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatestplus.play.{BrowserInfo, PortNumber, WsScalaTestClient}
import play.api.libs.json.JsValue
import play.api.libs.ws.{WSAPI, WSClient, WSRequest}
import play.api.libs.ws.ahc.{AhcConfigBuilder, AhcWSClient}

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext




/**
  * Base class for functional tests.
  *
  * By default, runs against a small subset of browsers. Set envvar
  * TEST_BROWSERS or system property test.browsers to a comma-separated
  * list of names from browserMappings.keys
  *
  * (envvar probably works best through activator/SBT - system properties
  * don't seem to be passed through.)
  */
protected abstract class FuncTestBase
  extends CommonFuncTestBase
  with SelectBrowsersPerSuite {

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
  lazy val notifications = PathPage("/notifications")
  lazy val activity = PathPage("/activity")

  lazy val standardSize = Dimension(1024, 768)
  // virtual dimension
  lazy val iphone5Size = Dimension(320, 568)

  setCaptureDir("target/functional-test/screenshots")

  def scrollTo(x: Int, y: Int) = executeScript(s"window.scrollTo($x, $y)")
  def scrollBy(x: Int, y: Int) = executeScript(s"window.scrollBy($x, $y)")

  private val soonPatience = PatienceConfig(
    timeout = scaled(Span(2, Seconds)),
    interval = scaled(Span(50, Millis))
  )

  def soon(fn: => Any)(implicit pos: Position) = eventually(fn)(soonPatience, pos)
}

/**
  * Starts a server but doesn't drive any browsers - use the
  * ws* methods to make calls to controllers and check the response.
  *
  * This currently doesn't work because it needs to start an embedded Play app,
  * which uses jclouds which needs an old version of Guava, but Selenium
  * needs a newer version of Guava.
  *
  * We don't need Selenium for the API tests but we would for our other embedded app tests.
  */
abstract class ApiFuncTestBase
  extends CommonFuncTestBase
    with EmbeddedServerConfig
    with WsScalaTestClient
    with WithActorSystem { // needed for WithWebClient

  // for WsScalaTestClient
  implicit def ws = web.client

}




