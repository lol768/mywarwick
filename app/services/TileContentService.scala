package services

import java.io.{IOException, InputStreamReader}
import javax.inject.Inject

import com.fasterxml.jackson.core.JsonParseException
import com.google.common.base.Charsets
import com.google.common.io.CharStreams
import com.google.inject.ImplementedBy
import models.{API, TileInstance}
import org.apache.http.client.methods._
import org.apache.http.conn.HttpHostConnectException
import org.apache.http.entity.{ContentType, StringEntity}
import org.apache.http.impl.client.HttpClients
import play.api.libs.json.{JsObject, _}
import system.{Logging, ThreadPools}
import uk.ac.warwick.sso.client.trusted.{CurrentApplication, TrustedApplicationUtils}
import warwick.sso.User

import scala.concurrent.Future

@ImplementedBy(classOf[TileContentServiceImpl])
trait TileContentService {

  def getTileContent(user: Option[User], tileInstance: TileInstance): Future[API.Response[JsObject]]

}

class TileContentServiceImpl @Inject()(
  trustedApp: CurrentApplication
) extends TileContentService with Logging {

  // TODO inject a client properly
  val client = HttpClients.createDefault()

  import ThreadPools.tileData

  // TODO cache
  override def getTileContent(user: Option[User], tileInstance: TileInstance): Future[API.Response[JsObject]] = Future {
    val request = jsonPost(tileInstance.tile.fetchUrl, tileInstance.options)
    user.foreach(user => signRequest(trustedApp, user.usercode.string, request))

    var response: CloseableHttpResponse = null

    try {
      response = client.execute(request)
      val body = CharStreams.toString(new InputStreamReader(response.getEntity.getContent, Charsets.UTF_8))
      val apiResponse  = Json.parse(body).as[API.Response[JsObject]]

      if (!apiResponse.success) {
        logger.logger.warn(s"Content provider returned Failure: user=${user.map(_.usercode.string).getOrElse("anonymous")}, tile=${tileInstance.tile.id}, response=$body")
      }

      apiResponse
    } catch {
      case e: HttpHostConnectException => API.Failure("error", Seq(API.Error("io", "Couldn't connect to provider")))
      case e @ (_:JsonParseException | _:JsResultException) => API.Failure("error", Seq(API.Error("parse", s"Parse error: ${e.getMessage}")))
      case e: IOException => API.Failure("error", Seq(API.Error("io", s"I/O error: ${e.getClass.getName}")))
    } finally {
      if (response != null) response.close()
    }
  }

  // For test overriding - if we cared that this was lame we could pull all TA ops
  // out into a service, no object functions
  def signRequest(trustedApp: CurrentApplication, usercode: String, request: HttpUriRequest) =
    TrustedApplicationUtils.signRequest(trustedApp, usercode, request)

  private def jsonPost(url: String, postData: Option[JsObject]) = {
    val request = new HttpPost(url)
    postData.foreach(data => request.setEntity(new StringEntity(Json.stringify(data), ContentType.APPLICATION_JSON)))
    request
  }

}
