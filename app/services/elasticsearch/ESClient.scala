package services.elasticsearch

import javax.inject.{Inject, Singleton}

import com.google.inject.ImplementedBy
import org.apache.http.HttpHost
import org.apache.http.client.config.RequestConfig
import org.apache.http.config.ConnectionConfig
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.elasticsearch.client.{RestClient, RestClientBuilder, RestHighLevelClient}
import play.api.Configuration

@ImplementedBy(classOf[ESClientConfigImpl])
trait ESClientConfig {
  def nodes: Seq[ESNode]

  def highLevelClient: RestHighLevelClient

  def lowLevelClient: RestClient
  
  def clogsClient: RestHighLevelClient
}

@Singleton
class ESClientConfigImpl @Inject()(
  config: Configuration
) extends ESClientConfig {

  val allHttpHosts: Seq[HttpHost] = this.nodes.map(node => new HttpHost(node.node, node.port, node.protocol))

  val lowLevelBuilder: RestClientBuilder = RestClient
    .builder(allHttpHosts.toArray: _*)
    .setMaxRetryTimeoutMillis(60000)
    .setHttpClientConfigCallback((configBuilder: HttpAsyncClientBuilder) => {
      configBuilder
        .setDefaultRequestConfig(
          RequestConfig.custom()
            .setSocketTimeout(60000)
            .setConnectTimeout(60000)
            .build()
        )
        .setMaxConnPerRoute(50)
        .setMaxConnTotal(200)
    })
  // NEWSTART-1377 with the increase MaxConns we might be able to resolve this,
  // if it comes up again, we might need to add some retry logic so it will try again later.

  val highLevel: RestHighLevelClient = new RestHighLevelClient(lowLevelBuilder)

  override def nodes: Seq[ESNode] =
    ESNode.fromConfigs(config
      .get[Seq[String]]("es.nodes"))

  override def highLevelClient: RestHighLevelClient = highLevel

  override def lowLevelClient: RestClient = highLevel.getLowLevelClient
  
  override def clogsClient: RestHighLevelClient = {
    val clogsNodes = ESNode
      .fromConfigs(config.get[Seq[String]]("clogs.nodes"))
      .map(node => new HttpHost(node.node, node.port, node.protocol))
    
    val clogsLowLevelBuilder = RestClient
      .builder(clogsNodes.toArray: _*)
      .setMaxRetryTimeoutMillis(60000)
      .setHttpClientConfigCallback((configBuilder: HttpAsyncClientBuilder) => {
        configBuilder
          .setDefaultRequestConfig(
            RequestConfig.custom()
              .setSocketTimeout(60000)
              .setConnectTimeout(60000)
              .build()
          )
          .setMaxConnPerRoute(50)
          .setMaxConnTotal(200)
      })
    
    new RestHighLevelClient(clogsLowLevelBuilder)
  }
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
