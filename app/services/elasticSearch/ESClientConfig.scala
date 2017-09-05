package services.elasticSearch

import javax.inject.{Inject, Singleton}

import com.google.inject.ImplementedBy
import com.sksamuel.elastic4s.{ElasticsearchClientUri, TcpClient}
import play.api.Configuration

import scala.collection.JavaConversions

@ImplementedBy(classOf[ESClientConfigImpl])
trait ESClientConfig {
  def getUriString(): String

  def getElastic4sTcpClient(): TcpClient
}

@Singleton
class ESClientConfigImpl @Inject()(
  config: Configuration
) extends ESClientConfig {

  override def getUriString(): String = {
    "elasticsearch://" +
      JavaConversions.asScalaBuffer(
        config
          .getConfigList("es.nodes")
          .getOrElse(throw new IllegalStateException("ElasticSearch nodes not configured - check es.nodes"))
      ).toList.map(e => {
        val host = e.getString("host") getOrElse (throw new IllegalStateException("ElasticSearch host is missing - check es.nodes"))
        val port = e.getInt("port").getOrElse(throw new IllegalStateException("ElasticSearch port number is missing - check es.nodes"))
        s"${host}:${port}"
      })
  }

  override def getElastic4sTcpClient(): TcpClient = TcpClient.transport(getUriString())
}