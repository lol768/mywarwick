package services.elasticSearch

import java.net.InetAddress
import javax.inject.{Inject, Singleton}

import com.google.inject.ImplementedBy
import org.apache.http.HttpHost
import org.elasticsearch.client.{RestClient, RestHighLevelClient}
import play.api.Configuration

import scala.collection.JavaConversions

@ImplementedBy(classOf[ESClientConfigImpl])
trait ESClientConfig {
  def nodes: Seq[ESNode]

  def newClient: RestHighLevelClient
}

@Singleton
class ESClientConfigImpl @Inject()(
  config: Configuration
) extends ESClientConfig {

  override def nodes: List[ESNode] = JavaConversions.asScalaBuffer(
    config
      .getConfigList("es.nodes")
      .getOrElse(throw new IllegalStateException("ElasticSearch nodes not configured - check es.nodes"))
  ).toList.map(e => {
    ESNode(
      e.getString("host") getOrElse (throw new IllegalStateException("ElasticSearch host is missing - check es.nodes")),
      e.getInt("port").getOrElse(throw new IllegalStateException("ElasticSearch port number is missing - check es.nodes"))
    )
  })

  override def newClient: RestHighLevelClient = {
    val allHttpHosts = this.nodes.map(node => new HttpHost(node.node, node.port, "http"))
    val javaArray = new Array[HttpHost](allHttpHosts.size)
    allHttpHosts.foreach(host => javaArray(allHttpHosts.indexOf(host)) = host)
    new RestHighLevelClient(RestClient.builder(javaArray: _*))
  }
}

case class ESNode(node: String, port: Int)