package services.elasticsearch

import javax.inject.Inject

import helpers.TestApplications
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.{DefaultActionBuilder, Results}
import play.api.routing.Router.Routes
import play.api.routing.{Router, SimpleRouter}
import play.api.test.{Helpers, TestServer}
import play.api.routing.sird._

/**
  * Runs a server that poorly mimics an Elastic instance.
  *
  * Has only been written for one specific test so if we use it for other things,
  * may need refactoring to support other cases.
  */
object MockElastic {
  class ElasticRouter @Inject() (Action: DefaultActionBuilder) extends SimpleRouter {
    import Results._
    override def routes: Routes = {
      case POST(p"/_bulk") => Action(Ok(Json.obj()))
    }
  }

  case class ElasticInfo(port: Int)

  def elasticApp = TestApplications.minimalBuilder
  .overrides(bind[Router].to[ElasticRouter])
  .build()

  // Could vary this per run if there were clashes.
  private val port = 49200


  def running(fn: ElasticInfo => Unit): Unit =
    Helpers.running(TestServer(port, elasticApp))(fn(ElasticInfo(port)))
}
