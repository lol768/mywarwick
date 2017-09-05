package services.elasticSearch

import java.net.InetAddress

import com.google.inject.{ImplementedBy, Inject}
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.transport.client.PreBuiltTransportClient
import play.api.Configuration

import scala.collection.JavaConversions

@ImplementedBy(classOf[esServiceImpl])
trait ESService {
  def index(index: String, docType: String, jsonString: String)
}

class esServiceImpl @Inject()(
  config: Configuration
) extends ESService {
  private val esNodes = JavaConversions.asScalaBuffer(
    config
      .getConfigList("es.nodes")
      .getOrElse(throw new IllegalStateException("ElasticSearch nodes not configured - check es.nodes"))
  ).toList.map(e => {
    ESClientConfig(
      e.getString("endpoint") getOrElse (throw new IllegalStateException("ElasticSearch endpoint is missing - check es.nodes")),
      e.getInt("port").getOrElse(throw new IllegalStateException("ElasticSearch port number is missing - check es.nodes"))
    )
  })

  private val client = new PreBuiltTransportClient(Settings.EMPTY)

  esNodes.foreach(node => {
    client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(node.endpoint), node.port))
  })

  override def index(index: String, docType: String, jsonString: String): Unit = {
    client.prepareIndex(index, docType).setSource(jsonString, XContentType.JSON).get()
  }
}