package services.elasticsearch

import com.google.inject.ImplementedBy
import javax.inject.{Inject, Singleton}
import javax.ws.rs.HttpMethod
import org.elasticsearch.client.{Response, RestClient}
import play.api.libs.json.JsValue

import scala.concurrent.Future
import scala.language.implicitConversions

@ImplementedBy(classOf[ElasticSearchAdminServiceImpl])
trait ElasticSearchAdminService {

  def putTemplate(template: JsValue, name: String): Future[Response]

  def deleteTemplate(name: String): Future[Response]

  def getTemplate(name: String): Future[Response]

  def getAllTemplates(): Future[Response]

  def hasTemplate(name: String): Future[Response]

  def getIndex(name: String): Future[Response]

  def getAllIndices(): Future[Response]
  //TODO other options that we need

}

@Singleton
class ElasticSearchAdminServiceImpl @Inject()(
  eSClientConfig: ESClientConfig
) extends ElasticSearchAdminService with LowLevelClientHelper {

  val lowLevelClient: RestClient = eSClientConfig.lowLevelClient

  implicit def getClient(client: Option[RestClient]): RestClient = client match {
    case Some(c: RestClient) => c
    case _ => this.lowLevelClient
  }

  override def putTemplate(
    template: JsValue,
    name: String
  ): Future[Response] = {
    performRequestAsync(
      method = HttpMethod.PUT,
      path = s"$templateRootPath/$name",
      entity = Some(httpEntityFromJsValue(template)),
      lowLevelClient = lowLevelClient
    )
  }

  override def deleteTemplate(
    name: String
  ): Future[Response] = {
    performRequestAsync(
      method = HttpMethod.DELETE,
      path = s"$templateRootPath/$name",
      lowLevelClient = lowLevelClient
    )
  }

  override def getTemplate(
    name: String
  ): Future[Response] = {
    performRequestAsync(
      method = HttpMethod.GET,
      path = s"$templateRootPath/$name",
      lowLevelClient = lowLevelClient
    )
  }

  override def getAllTemplates(): Future[Response] = {
    getTemplate("")
  }

  override def hasTemplate(
    name: String
  ): Future[Response] = {
    performRequestAsync(
      method = HttpMethod.HEAD,
      path = s"$templateRootPath/$name",
      lowLevelClient = lowLevelClient
    )
  }

  override def getIndex(name: String) = {
    performRequestAsync(
      HttpMethod.GET,
      path = s"/$name",
      lowLevelClient = lowLevelClient
    )
  }

  override def getAllIndices() = {
    getIndex("*")
  }
}