package helpers

import org.scalatest.Suite
import org.scalatestplus.play.OneAppPerSuite
import play.api.inject._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import play.api.{Configuration, Environment}
import warwick.sso._

trait OneStartAppPerSuite extends Suite with OneAppPerSuite {

  lazy val config = Configuration.load(Environment.simple(), Map("config.file" -> "test/test.conf"))

  implicit override lazy val app =
    new GuiceApplicationBuilder(configuration = config)
      .in(Environment.simple())
      .configure(inMemoryDatabase("default", Map("MODE" -> "Oracle")))
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

}
