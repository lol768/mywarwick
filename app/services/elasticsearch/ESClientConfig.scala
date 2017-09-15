package services.elasticsearch

import javax.inject.{Inject, Singleton}

import com.google.inject.ImplementedBy
import org.apache.http.HttpHost
import org.elasticsearch.client.{RestClient, RestHighLevelClient}
import play.api.Configuration

@ImplementedBy(classOf[ESClientConfigImpl])
trait ESClientConfig {
  def nodes: Seq[ESNode]

  def newClient: RestHighLevelClient
}

@Singleton
class ESClientConfigImpl @Inject()(
  config: Configuration
) extends ESClientConfig {

  override def nodes: Seq[ESNode] = config
    .getConfigSeq("es.nodes")
    .getOrElse(throw new IllegalStateException("ElasticSearch nodes not configured - check es.nodes"))
    .map(e => {
      ESNode(
        e.getString("host") getOrElse (throw new IllegalStateException("ElasticSearch host is missing - check es.nodes")),
        e.getInt("port").getOrElse(throw new IllegalStateException("ElasticSearch port number is missing - check es.nodes")),
        e.getString("protocol").getOrElse(throw new IllegalStateException("ElasticSearch protocol is missing - check es.nodes"))
      )
    })

  override def newClient: RestHighLevelClient = {
    val allHttpHosts: Seq[HttpHost] = this.nodes.map(node => new HttpHost(node.node, node.port, node.protocol))
    new RestHighLevelClient(RestClient.builder(allHttpHosts.toArray: _*))
  }
}

case class ESNode(node: String, port: Int, protocol: String)