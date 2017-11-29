package services.elasticsearch

import javax.inject.{Inject, Singleton}

import com.google.inject.ImplementedBy
import org.apache.http.HttpHost
import org.elasticsearch.client.{RestClient, RestClientBuilder, RestHighLevelClient}
import play.api.Configuration

@ImplementedBy(classOf[ESClientConfigImpl])
trait ESClientConfig {
  def nodes: Seq[ESNode]

  def highLevelClient: RestHighLevelClient

  def lowLevelClient: RestClient
}

@Singleton
class ESClientConfigImpl @Inject()(
  config: Configuration
) extends ESClientConfig {

  val allHttpHosts: Seq[HttpHost] = this.nodes.map(node => new HttpHost(node.node, node.port, node.protocol))
  val lowLevel: RestClientBuilder = RestClient.builder(allHttpHosts.toArray: _*).setMaxRetryTimeoutMillis(60000)
  val highLevel: RestHighLevelClient = new RestHighLevelClient(lowLevel)

  override def nodes: Seq[ESNode] =
    ESNode.fromConfigs(config
      .get[Seq[String]]("es.nodes"))

  override def highLevelClient: RestHighLevelClient = highLevel

  override def lowLevelClient: RestClient = highLevel.getLowLevelClient
}

case class ESNode(node: String, port: Int, protocol: String = "http")

object ESNode {
  def fromSingleConfigString(config: String): ESNode = {
    val configArr = config.split(":")
    ESNode(configArr(0).trim, configArr(1).trim.toInt)
  }

  def fromConfigs(configs: Seq[String]): Seq[ESNode] = {
    configs.map(fromSingleConfigString)
  }
}