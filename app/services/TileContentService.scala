package services

import javax.inject.Inject

import com.fasterxml.jackson.core.JsonParseException
import com.google.inject.ImplementedBy
import models.{API, TileInstance}
import org.apache.http.client.methods.{HttpPost, HttpUriRequest}
import org.apache.http.entity.StringEntity
import org.apache.http.client.methods.{HttpUriRequest, HttpPost}
import org.apache.http.entity.{ContentType, StringEntity}
import org.apache.http.impl.client.HttpClients
import play.api.libs.json.{JsObject, _}
import system.Threadpools
import uk.ac.warwick.sso.client.trusted.{CurrentApplication, TrustedApplicationUtils}
import warwick.sso.User

import scala.concurrent.Future

@ImplementedBy(classOf[TileContentServiceImpl])
trait TileContentService {

  def getTileContent(tileInstance: TileInstance): Future[JsObject]
  def getTileContent(user: Option[User], tileInstance: TileInstance): Future[API.Response[JsObject]]

}

class TileContentServiceImpl @Inject()(
  trustedApp: CurrentApplication
) extends TileContentService {

  // TODO inject a client properly
  val client = HttpClients.createDefault()

  override def getTileContent(tileInstance: TileInstance): Future[JsObject] =
    Future.successful(Json.obj())

  import Threadpools.tileData

  // TODO cache
  override def getTileContent(user: Option[User], tileInstance: TileInstance): Future[API.Response[JsObject]] = Future {
    val request = jsonPost(tileInstance.tile.fetchUrl, tileInstance.options.getOrElse(Json.obj()))
    val usercode = user.map(_.usercode.string).orNull
    signRequest(trustedApp, usercode, request)
    val response = client.execute(request)
    try {
      Json.parse(response.getEntity.getContent).as[API.Response[JsObject]]
    } catch {
      // TODO: gracefully handle dodgy fetch urls
            case jpe: JsonParseException => API.Failure("", Seq(API.Error("0", "Could not parse json from Tile fetch url")))
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
