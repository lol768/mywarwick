package services.elasticSearch

import java.net.InetAddress
import javax.inject.{Inject, Singleton}

import com.google.inject.ImplementedBy
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.transport.client.PreBuiltTransportClient
import play.api.Configuration

import scala.collection.JavaConversions

@ImplementedBy(classOf[ESClientConfigImpl])
trait ESClientConfig {
  def nodes: Seq[ESNode]

  def newTransportClient: PreBuiltTransportClient
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
      e.getString("endpoint") getOrElse (throw new IllegalStateException("ElasticSearch endpoint is missing - check es.nodes")),
      e.getInt("port").getOrElse(throw new IllegalStateException("ElasticSearch port number is missing - check es.nodes"))
    )
  })

  override def newTransportClient: PreBuiltTransportClient = {
    val client = new PreBuiltTransportClient(Settings.EMPTY)
    this.nodes.foreach(node => {
      client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(node.node), node.port))
    })
    client
  }
}

case class ESNode(node: String, port: Int)