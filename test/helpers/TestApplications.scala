package helpers

import org.databrary.PlayLogbackAccessModule
import play.api.db.evolutions.{ClassLoaderEvolutionsReader, EvolutionsReader}
import play.api.inject._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.routing.{Router, SimpleRouter}
import play.api.test.Helpers._
import play.api.{Configuration, Environment}
import services.elasticsearch.{ActivityESService, ESClientConfig}
import services.messaging.MobileOutputService
import services.{CookieSSOClient, NullMobileOutputService, SchedulerService}
import system.{DatabaseDialect, H2DatabaseDialect, NullCacheModule}
import warwick.accesslog.LogbackAccessModule
import warwick.sso._


object TestApplications {

  def testConfig(environment: Environment) =
    config("test.conf", environment)

  def functionalConfig(environment: Environment) =
    config("functional.conf", environment)

  def config(file: String, environment: Environment) =
    Configuration.load(environment, Map("config.resource" -> file))

  def minimalBuilder = GuiceApplicationBuilder(loadConfiguration = e => config("minimal.conf", e))
    .in(Environment.simple())
    .disable[PlayLogbackAccessModule]
    .disable[LogbackAccessModule]

  /**
    * As minimal an Application as can be created. Use for any tests
    * where you just can't do without an Application, like something that
    * requires WSAPI which is a pain to build by hand.
    */
  def minimal() =
    minimalBuilder
      .router(Router.empty)
      .build()

  def fullBuilder(user: Option[User]) =
    GuiceApplicationBuilder(loadConfiguration = testConfig)
      .in(Environment.simple())
      .configure(inMemoryDatabase("default", Map("MODE" -> "Oracle")))
      .bindings(
        bind[LoginContext].toInstance(new MockLoginContext(user))
      )
      .disable[PlayLogbackAccessModule]
      .overrides(
        bind[SSOClient].to[MockSSOClient],
        bind[DatabaseDialect].to[H2DatabaseDialect],
        bind[SchedulerService].to[MockSchedulerService],
        bind[ActivityESService].to[MockActivityESService],

        // Allows putting test versions of migrations under test/resources/evolutions/default
        bind[EvolutionsReader].toInstance(new ClassLoaderEvolutionsReader())
      )

  /**
    * As full an Application as can be created while still talking to
    * mock external services only, and an in-memory database. Used for
    * DAO tests and integration tests.
    */
  def full(user: Option[User] = None) =
    fullBuilder(user).build()

  /**
    * The full application, but with no routes - this typically means none of the
    * controllers are created or injected, and any services only referenced by
    * controllers will not be immediately created either.
    */
  def fullNoRoutes(user: Option[User] = None) =
    fullNoRoutesBuilder(user)
        .build()

  def fullNoRoutesBuilder(user: Option[User]) =
    fullBuilder(user)
      .router(Router.empty)

  def functional() =
    GuiceApplicationBuilder(loadConfiguration = functionalConfig)
      .in(Environment.simple())
      .configure(inMemoryDatabase("default", Map("MODE" -> "Oracle")))
      .disable[PlayLogbackAccessModule]
      .overrides(
        bind[SSOClient].to[CookieSSOClient],
        bind[DatabaseDialect].to[H2DatabaseDialect],
        bind[MobileOutputService].to[NullMobileOutputService],

        // Allows putting test versions of migrations under test/resources/evolutions/default
        bind[EvolutionsReader].toInstance(new ClassLoaderEvolutionsReader())
      )
      .build()

}
