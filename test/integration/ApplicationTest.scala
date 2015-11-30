package integration

import controllers.HomeController
import helpers.OneStartAppPerSuite
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.Configuration
import play.api.Environment
import play.api.inject._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import warwick.sso._

/**
  * Eventually we'll have a suite of integration tests, that actually
  * drive the app and test what happens. For now this is good just to
  * check that the application starts and isn't horribly misconfigured.
  */
class ApplicationTest extends PlaySpec with OneStartAppPerSuite {

  "The application" should {

    "start up successfully" in {

      val c = app.injector.instanceOf[HomeController]

    }

  }

}
