package services.elasticsearch

import java.util
import java.util.Collections

import org.apache.http.entity.ContentType
import org.apache.http.nio.entity.NStringEntity
import org.elasticsearch.client.{Response, ResponseListener, RestClient}
import play.api.libs.json.{JsValue, Json}
import warwick.core.Logging

import scala.concurrent.{Future, Promise}

trait ElasticSearchAdminServiceHelper extends Logging {

  val lowLevelClient: RestClient

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

  def performRequestAsync(
    method: String,
    path: String,
    param: util.Map[String, String] = emptyParam,
    entity: Option[NStringEntity] = None,
    responseListener: (Promise[Response], (Option[Any], Promise[Response]) => Unit) => ResponseListener = responseListener,
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
}