package services.elasticsearch

import javax.inject.Inject

import org.elasticsearch.action.bulk.BulkRequest
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.common.xcontent.XContentFactory
import org.scalatestplus.play.PlaySpec
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc.DefaultActionBuilder
import play.api.routing.Router.Routes
import play.api.routing.sird._
import play.api.mvc.Results.Ok

class ESClientConfigTest @Inject()(Action: DefaultActionBuilder) extends PlaySpec {
  "ESClientConfigTest" should {

    "submit a request successfully" in {
      // NEWSTART-1253 httpclient was rejecting request with "bad MIME type".
      // fixed in upgraded version of elasticsearch client (6.0.0).

      val routes: Routes = {
        case POST(p"/_bulk") => Action(Ok(Json.obj()))
      }

      MockElastic.running(routes) { info =>
        val config = new ESClientConfigImpl(Configuration(
          "es.nodes" -> Seq(s"localhost:${info.port}")
        ))

        val content = XContentFactory.jsonBuilder().startObject.endObject
        val writeReq = new IndexRequest("tweets", "tweet", "1").source(content)
        val req = new BulkRequest().add(writeReq)
        config.highLevelClient.bulk(req, RequestOptions.DEFAULT)
      }
    }
  }
}
