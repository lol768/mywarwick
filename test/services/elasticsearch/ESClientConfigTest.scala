package services.elasticsearch

import javax.inject.Inject

import helpers.TestApplications
import org.elasticsearch.action.bulk.BulkRequest
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.common.xcontent.XContentFactory
import org.scalatest.concurrent.{PatienceConfiguration, ScalaFutures}
import org.scalatestplus.play.PlaySpec
import play.api.Configuration
import play.api.inject._
import play.api.libs.json.Json
import play.api.mvc._
import play.api.routing.Router.Routes
import play.api.routing.sird._
import play.api.routing.{Router, SimpleRouter}
import play.api.test.{Helpers, TestServer}

class ESClientConfigTest extends PlaySpec with ScalaFutures with PatienceConfiguration {
  "ESClientConfigTest" should {

    "submit a request successfully" in {
      // NEWSTART-1253 httpclient was rejecting request with "bad MIME type".
      // fixed in upgraded version of elasticsearch client (6.0.0).

      MockElastic.running { info =>
        val config = new ESClientConfigImpl(Configuration(
          "es.nodes" -> Seq(s"localhost:${info.port}")
        ))

        val content = XContentFactory.jsonBuilder().startObject.endObject
        val writeReq = new IndexRequest("tweets", "tweet", "1").source(content)
        val req = new BulkRequest().add(writeReq)
        config.highLevelClient.bulk(req)
      }
    }

  }
}
