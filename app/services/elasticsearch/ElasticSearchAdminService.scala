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

  // template
  def putTemplate(template: JsValue, name: String, lowLevelClient: Option[RestClient] = None): Future[Response]

  def deleteTemplate(name: String, lowLevelClient: Option[RestClient] = None): Future[Response]

  def getTemplate(name: String, lowLevelClient: Option[RestClient] = None): Future[Response]

  def hasTemplate(name: String, lowLevelClient: Option[RestClient] = None): Future[Response]

  // remove index

  // etc.

}

@Singleton
class ElasticSearchAdminServiceImpl @Inject()(
  eSClientConfig: ESClientConfig
) extends ElasticSearchAdminService with ElasticSearchAdminServiceHelper {

  lazy val lowLevelClient: RestClient = eSClientConfig.newClient.getLowLevelClient

  def performRequestAsync(
    method: String,
    path: String,
    param: util.Map[String, String] = Collections.emptyMap()[String, String],
    entity: Option[NStringEntity] = None,
    responseListener: (Promise[Response], (Option[Any], Promise[Response]) => Unit) => ResponseListener = super.responseListener,
    lowLevelClient: Option[RestClient] = None
  ): Future[Response] = {

    val client: RestClient = lowLevelClient match {
      case Some(c: RestClient) => c
      case _ => this.lowLevelClient
    }

    val responsePromise: Promise[Response] = Promise[Response]
    entity match {
      case Some(e: NStringEntity) =>
        client.performRequestAsync(
          method,
          path,
          param,
          e,
          responseListener(responsePromise)
        )
      case _ =>
        client.performRequestAsync(
          method,
          path,
          param,
          responseListener(responsePromise)
        )
    }
    responsePromise.future
  }

  override def putTemplate(
    template: JsValue,
    name: String,
    lowLevelClient: Option[RestClient] = None
  ): Future[Response] = {
    performRequestAsync(
      method = Method.get,
      path = s"$templateRootPath/$name",
      param = emptyParam,
      entity = Some(httpEntityFromJsValue(template)),
      lowLevelClient = lowLevelClient
    )
  }

  override def deleteTemplate(
    name: String,
    lowLevelClient: Option[RestClient] = None
  ): Future[Response] = {
    performRequestAsync(
      method = Method.delete,
      path = s"$templateRootPath/$name",
      lowLevelClient = lowLevelClient
    )
  }

  override def getTemplate(
    name: String,
    lowLevelClient: Option[RestClient] = None
  ): Future[Response] = {
    performRequestAsync(
      method = Method.get,
      path = s"$templateRootPath/$name",
      lowLevelClient = lowLevelClient
    )
  }

  override def hasTemplate(
    name: String,
    lowLevelClient: Option[RestClient] = None
  ): Future[Response] = {
    performRequestAsync(
      method = Method.head,
      path = s"$templateRootPath/$name",
      lowLevelClient = lowLevelClient
    )
  }
}

trait ElasticSearchAdminServiceHelper extends Logging {

  val templateRootPath = "_template"

  object Method {
    val put = "PUT"
    val delete = "DELETE"
    val post = "POST"
    val get = "GET"
    val head = "HEAD"
  }

  val emptyParam: util.Map[String, String] = Collections.emptyMap()[String, String]

  def httpEntityFromJsValue(json: JsValue) = new NStringEntity(Json.stringify(json), ContentType.APPLICATION_JSON)

  def responseHandler(
    res: Option[Any],
    responsePromise: Promise[Response]
  ): Unit = {
    res match {
      case Some(e: Exception) =>
        responsePromise.failure(e)
        logger.error("Exception thrown after sending request to elasticsearch", e)
      case Some(r: Response) =>
        responsePromise.success(r)
        logger.debug(s"Response received from elasticsearch: ${r.toString}")
    }
  }

  def responseListener(
    responsePromise: Promise[Response],
    callbackHandler: (Option[Any], Promise[Response]) => Unit = this.responseHandler
  ): ResponseListener = {
    new ResponseListener() {
      def onFailure(exception: Exception): Unit = {
        callbackHandler(Some(exception), responsePromise)
      }

      override def onSuccess(response: Response): Unit = {
        callbackHandler(Some(response), responsePromise)
      }
    }
  }

}

object ElasticSearchAdminServiceHelper extends ElasticSearchAdminServiceHelper