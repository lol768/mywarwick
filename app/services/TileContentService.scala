package services

import java.io.IOException
import java.nio.charset.Charset
import javax.inject.Inject

import com.google.common.base.Charsets
import com.google.inject.ImplementedBy
import models.TileInstance$
import play.api.libs.json.{Json, JsObject}
import models.{API, TileInstance}
import org.apache.http.client.methods.{HttpUriRequest, HttpPost}
import org.apache.http.entity.{ContentType, StringEntity}
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import play.api.libs.json.{Writes, Json, JsObject}
import play.api.libs.ws.{WSAPI, WS}
import system.Threadpools
import uk.ac.warwick.sso.client.trusted.{CurrentApplication, TrustedApplicationUtils, TrustedApplicationsManager}
import warwick.sso.User

import scala.concurrent.Future

@ImplementedBy(classOf[TileContentServiceImpl])
trait TileContentService {

  def getTileContent(tileInstance: TileInstance): Future[JsObject]
  def getTileContent(user: Option[User], tileInstance: TileInstance): Future[API.Response[JsObject]]

}

class TileContentServiceImpl @Inject() (
  trustedApp: CurrentApplication
) extends TileContentService {

  override def getTileContent(tileInstance: TileInstance): Future[JsObject] =
    Future.successful(Json.obj())
  // TODO inject a client properly
  val client = HttpClients.createDefault()

  import Threadpools.tileData

  // TODO cache
  override def getTileContent(user: Option[User], tileInstance: TileInstance): Future[API.Response[JsObject]] = Future {
    val request = jsonPost(tileInstance.tile.fetchUrl, tileInstance.options.getOrElse(Json.obj()))
    val usercode = user.map(_.usercode.string).orNull
    signRequest(trustedApp, usercode, request)
    val response = client.execute(request)
    try {
      Json.parse(response.getEntity.getContent).as[API.Response[JsObject]]
    } finally {
      response.close()
    }
  }

  // For test overriding - if we cared that this was lame we could pull all TA ops
  // out into a service, no object functions
  def signRequest(trustedApp: CurrentApplication, usercode: String, request: HttpUriRequest) =
    TrustedApplicationUtils.signRequest(trustedApp, usercode, request)

  private def jsonPost(url: String, postData: JsObject) = {
    val request = new HttpPost(url)
    request.setEntity(new StringEntity(Json.stringify(postData), ContentType.APPLICATION_JSON));
    request
  }

}
