package services.elasticsearch

import java.util
import java.util.Collections
import javax.inject.{Inject, Singleton}

import com.google.inject.ImplementedBy
import org.apache.http.nio.entity.NStringEntity
import org.elasticsearch.client.{Response, ResponseListener, RestClient}
import play.api.libs.json.{JsValue, Json}
import warwick.core.Logging
import org.apache.http.entity.ContentType

import scala.concurrent.{Future, Promise}

@ImplementedBy(classOf[ElasticSearchAdminServiceImpl])
trait ElasticSearchAdminService {

  def putTemplate(template: JsValue, name: String, suppliedLowLevelClient: Option[RestClient] = None): Future[Response]

  def deleteTemplate(name: String, suppliedLowLevelClient: Option[RestClient] = None): Future[Response]

  def getTemplate(name: String, suppliedLowLevelClient: Option[RestClient] = None): Future[Response]

  def hasTemplate(name: String, suppliedLowLevelClient: Option[RestClient] = None): Future[Response]

  //TODO other options that we need

}

@Singleton
class ElasticSearchAdminServiceImpl @Inject()(
  eSClientConfig: ESClientConfig
) extends ElasticSearchAdminService with ElasticSearchAdminServiceHelper {

  lazy val lowLevelClient: RestClient = eSClientConfig.newClient.getLowLevelClient

  implicit def getClient(client: Option[RestClient]): RestClient = client match {
    case Some(c: RestClient) => c
    case _ => this.lowLevelClient
  }

  override def putTemplate(
    template: JsValue,
    name: String,
    suppliedLowLevelClient: Option[RestClient] = None
  ): Future[Response] = {
    performRequestAsync(
      method = Method.get,
      path = s"$templateRootPath/$name",
      entity = Some(httpEntityFromJsValue(template)),
      lowLevelClient = suppliedLowLevelClient
    )
  }

  override def deleteTemplate(
    name: String,
    suppliedLowLevelClient: Option[RestClient] = None
  ): Future[Response] = {
    performRequestAsync(
      method = Method.delete,
      path = s"$templateRootPath/$name",
      lowLevelClient = suppliedLowLevelClient
    )
  }

  override def getTemplate(
    name: String,
    suppliedLowLevelClient: Option[RestClient] = None
  ): Future[Response] = {
    performRequestAsync(
      method = Method.get,
      path = s"$templateRootPath/$name",
      lowLevelClient = suppliedLowLevelClient
    )
  }

  override def hasTemplate(
    name: String,
    suppliedLowLevelClient: Option[RestClient] = None
  ): Future[Response] = {
    performRequestAsync(
      method = Method.head,
      path = s"$templateRootPath/$name",
      lowLevelClient = suppliedLowLevelClient
    )
  }
}