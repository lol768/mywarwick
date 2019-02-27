package services.elasticsearch

import java.util
import java.util.Collections

import org.apache.http.entity.ContentType
import org.apache.http.nio.entity.NStringEntity
import org.elasticsearch.client.{Request, Response, RestClient}

import warwick.core.Logging
import collection.JavaConverters._
import scala.concurrent.Future
import play.api.libs.json.{JsObject, JsValue, Json}

trait LowLevelClientHelper extends Logging {

  val templateRootPath = "/_template"

  def countPathForIndexName(path: String) = s"/$path/_count"

  def getCountFromCountApiRes(res: Response): Int = (Json
    .parse(scala.io.Source.fromInputStream(res.getEntity.getContent).mkString) \ "count")
    .get
    .toString()
    .toInt

  val emptyParam: util.Map[String, String] = Collections.emptyMap()

  def httpEntityFromJsValue(json: JsValue) = new NStringEntity(Json.stringify(json), ContentType.APPLICATION_JSON)

  def performRequestAsync(
    method: String,
    path: String,
    lowLevelClient: RestClient,
    suppliedParam: Option[Map[String, String]] = None,
    entity: Option[NStringEntity] = None
  ): Future[Response] = {
    val listener = new FutureResponseListener

    val request = new Request(method, path)
    suppliedParam.getOrElse(Map()).foreach { case (k, v) => request.addParameter(k, v) }
    entity.foreach(request.setEntity)

    lowLevelClient.performRequestAsync(request, listener)

    listener.future
  }
}

object LowLevelClientHelper extends LowLevelClientHelper