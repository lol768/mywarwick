package integration

import controllers.HomeController
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
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
class ApplicationTest extends PlaySpec with OneAppPerSuite {

  implicit override lazy val app = new GuiceApplicationBuilder()
    .in(Environment.simple())
    .configure(inMemoryDatabase())
    .bindings(
      bind[LoginContext].toInstance(new LoginContext {
        override val user: Option[User] = None
        override val actualUser: Option[User] = None

        override def loginUrl(target: Option[String]): String = "https://example.com/login"
      })
    )
    .overrides(
      // Fake SSOClient
      bind[SSOClient].to[MockSSOClient]
      // FIXME still numerous non-fake things, like UserLookup. Replace whole Guice module?
    )
    .build()

  "The application" should {

    "start up successfully" in {

      val c = app.injector.instanceOf[HomeController]

    }

  }

}
