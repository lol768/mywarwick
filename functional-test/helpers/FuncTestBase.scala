package helpers

import org.openqa
import org.openqa.selenium.WebDriver
import org.scalactic.source.Position
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatestplus.play.BrowserInfo

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
    webDriver.manage.window.setSize(new openqa.selenium.Dimension(d.width, d.height))
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
  lazy val notifications = PathPage("/alerts")
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

  /**
    * Version of `eventually` for things that shouldn't really take that long -
    * allows tests to fail faster if something has gone wrong, without having
    * to wait 15 seconds.
    */
  def soon(fn: => Any)(implicit pos: Position) = eventually(fn)(soonPatience, pos)
}






