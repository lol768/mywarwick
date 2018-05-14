package services.elasticsearch

import java.util
import java.util.Collections

import org.apache.http.entity.ContentType
import org.apache.http.nio.entity.NStringEntity
import org.elasticsearch.client.{Response, RestClient}
import play.api.libs.json.{JsObject, JsValue, Json}
import warwick.core.Logging

import collection.JavaConverters._
import scala.concurrent.Future

trait LowLevelClientHelper extends Logging {

  val templateRootPath = "/_template"

  def countPathForIndexName(path: String) = s"/$path/_count"

  def getCountFromCountApiRes(res: Response): Int = (Json
    .parse(scala.io.Source.fromInputStream(res.getEntity.getContent).mkString) \ "count")
    .get
    .toString()
    .toInt

  def makeQueryForCountApiFromActivityESSearchQuery(input: ActivityESSearch.SearchQuery): JsValue = JsObject(Seq(
    "query" -> JsObject(Seq(
      "bool" -> (Json.parse(ActivityESServiceSearchHelper.makeBoolQueryBuilder(input).toString) \ "bool").get
    ))
  ))

  def makePathForCountApiFromActivityEsSearchQuery(input: ActivityESSearch.SearchQuery): String = {
    countPathForIndexName(ActivityESServiceSearchHelper.indexNameForActivitySearchQuery(input))
  }

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

    val param = suppliedParam.getOrElse(Map()).asJava

    entity match {
      case Some(nStringEntity: NStringEntity) =>
        lowLevelClient.performRequestAsync(
          method,
          path,
          param,
          nStringEntity,
          listener
        )
      case _ =>
        lowLevelClient.performRequestAsync(
          method,
          path,
          param,
          listener
        )
    }

    listener.future
  }
}

object LowLevelClientHelper extends LowLevelClientHelper