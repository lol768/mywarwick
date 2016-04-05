package helpers

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures, ScaledTimeSpans}
import org.scalatest.{FreeSpec, _}
import org.scalatestplus.play.{BrowserInfo, WsScalaTestClient}

protected abstract class CommonFuncTestBase
  extends FreeSpec
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

  /**
    * Adds a browser-specific top level description to ensure
    * that all tests have a unique name.
    */
  override def describeBrowser(info: BrowserInfo)(block: => Unit) =
    s"Browser ${info.name}" - block

}

/**
  * Starts a server but doesn't drive any browsers - use the
  * ws* methods to make calls to controllers and check the response.
  */
abstract class ApiFuncTestBase extends CommonFuncTestBase
