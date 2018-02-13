package services.elasticsearch

import helpers.TestApplications
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.test.{Helpers, TestServer}

/**
  * Runs a server that poorly mimics an Elastic instance.
  *
  * Has only been written for one specific test so if we use it for other things,
  * may need refactoring to support other cases.
  */
object MockElastic {
  case class ElasticInfo(port: Int)

  def elasticApp(routes: Routes) = TestApplications.minimalBuilder
    .router(SimpleRouter(routes))
    .build()

  // Could vary this per run if there were clashes.
  private val port: Int = 49200

  def running(routes: Routes)(fn: ElasticInfo => Unit): Unit =
    Helpers.running(TestServer(port, elasticApp(routes)))(fn(ElasticInfo(port)))
}
