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

@ImplementedBy(classOf[ElasticSearchAdminServiceImpl])
trait ElasticSearchAdminService {

  val lowLevelClient: RestClient

  val templateRootPath = "_template"

  // template
  def putTemplate(template: JsValue, name: String, lowLevelClient: RestClient = lowLevelClient)

  def deleteTemplate(name: String, lowLevelClient: RestClient = lowLevelClient)

  def getTemplate(name: String, lowLevelClient: RestClient = lowLevelClient)

  def hasTemplate(name: String, lowLevelClient: RestClient = lowLevelClient)

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

  val responseListener = new ResponseListener() {
    def onFailure(exception: Exception): Unit = {

    }

    override def onSuccess(response: Response) = {

    }

  }

  override def putTemplate(template: JsValue, name: String, lowLevelClient: RestClient): Unit = {
    lowLevelClient.performRequestAsync(
      "PUT",
      s"$templateRootPath/$name",
      emptyParam,
      httpEntityFromJsValue(template),
      responseListener
    )
  }

  override def deleteTemplate(name: String, lowLevelClient: RestClient): Unit = ???

  override def getTemplate(name: String, lowLevelClient: RestClient): Unit = ???

  override def hasTemplate(name: String, lowLevelClient: RestClient): Unit = ???
}
