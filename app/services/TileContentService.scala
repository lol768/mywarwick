package services

import java.nio.charset.Charset
import javax.inject.Inject

import com.google.common.base.Charsets
import com.google.inject.ImplementedBy
import models.UserTile
import org.apache.http.client.methods.{HttpUriRequest, HttpPost}
import org.apache.http.entity.StringEntity
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

  def getTileContent(user: Option[User], userTile: UserTile): Future[JsObject]

}

class TileContentServiceImpl @Inject() (
  trustedApp: CurrentApplication
) extends TileContentService {

  // TODO inject a client properly
  val client = HttpClients.createDefault()

  import Threadpools.tileData

  // TODO cache
  override def getTileContent(user: Option[User], userTile: UserTile): Future[JsObject] = Future {
    val request = jsonPost(userTile.tile.fetchUrl, userTile.options.getOrElse(Json.obj()))
    val usercode = user.map(_.usercode.string).orNull
    signRequest(trustedApp, usercode, request)
    val response = client.execute(request)
    try {
      val content = response.getEntity.getContent
      Json.parse(content).as[JsObject]
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
    request.setEntity(new StringEntity(Json.stringify(postData)))
    request
  }

}
