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

  val lowLevelClient: RestClient

  val templateRootPath = "_template"

  // template
  def putTemplate(template: JsValue, name: String, lowLevelClient: RestClient = lowLevelClient): Future[Response]

  def deleteTemplate(name: String, lowLevelClient: RestClient = lowLevelClient): Future[Response]

  def getTemplate(name: String, lowLevelClient: RestClient = lowLevelClient): Future[Response]

  def hasTemplate(name: String, lowLevelClient: RestClient = lowLevelClient): Future[Response]

  // remove index

  // etc.

}

@Singleton
class ElasticSearchAdminServiceImpl @Inject()(
  eSClientConfig: ESClientConfig
) extends ElasticSearchAdminService with Logging {

  override lazy val lowLevelClient: RestClient = eSClientConfig.newClient.getLowLevelClient

  val emptyParam: util.Map[String, String] = Collections.emptyMap()[String, String]

  def httpEntityFromJsValue(json: JsValue) = new NStringEntity(Json.stringify(json), ContentType.APPLICATION_JSON)

  def defaultResponseHandler(res: Option[Any], responsePromise: Promise[Response]): Unit = {
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
    callbackHandler: (Option[Any], Promise[Response]) => Unit = defaultResponseHandler
  ): ResponseListener = {
    new ResponseListener() {
      def onFailure(exception: Exception): Unit = {
        callbackHandler(Some(exception), responsePromise)
      }

      override def onSuccess(response: Response) = {
        callbackHandler(Some(response), responsePromise)
      }
    }
  }

  override def putTemplate(
    template: JsValue,
    name: String,
    lowLevelClient: RestClient
  ): Future[Response] = {
    val responsePromise = Promise[Response]
    lowLevelClient.performRequestAsync(
      "PUT",
      s"$templateRootPath/$name",
      emptyParam,
      httpEntityFromJsValue(template),
      responseListener(responsePromise)
    )
    responsePromise.future
  }

  override def deleteTemplate(name: String, lowLevelClient: RestClient): Unit = {

  }

  override def getTemplate(name: String, lowLevelClient: RestClient): Unit = ???

  override def hasTemplate(name: String, lowLevelClient: RestClient): Unit = ???
}
