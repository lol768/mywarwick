package services

import java.io.{IOException, InputStreamReader}
import java.net.SocketTimeoutException

import javax.inject.{Inject, Named}
import com.fasterxml.jackson.core.JsonProcessingException
import com.google.common.base.Charsets
import com.google.common.io.CharStreams
import com.google.inject.ImplementedBy
import models.{API, Tile, TileInstance}
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods._
import org.apache.http.conn.HttpHostConnectException
import org.apache.http.entity.{ContentType, StringEntity}
import org.apache.http.impl.client.{CloseableHttpClient, HttpClientBuilder}
import play.api.Configuration
import play.api.cache._
import play.api.libs.json.{JsObject, _}
import play.api.libs.ws.{WSClient, WSRequest}
import system.Logging
import uk.ac.warwick.sso.client.trusted.{CurrentApplication, EncryptedCertificate, TrustedApplication, TrustedApplicationUtils}
import warwick.sso.{User, Usercode}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.Try

object TileContentService {

  def getRequestConfigForTile(tileInstance: TileInstance): RequestConfig = {
    RequestConfig
      .custom()
      .setConnectTimeout(tileInstance.tile.timeout)
      .setSocketTimeout(tileInstance.tile.timeout)
      .build()
  }

}

@ImplementedBy(classOf[TileContentServiceImpl])
trait TileContentService {
  def getTilesOptions(user: Option[User], tiles: Seq[Tile]): Future[JsValue]

  def getTileContent(user: Option[Usercode], tileInstance: TileInstance): Future[API.Response[JsObject]]
}

class TileContentServiceImpl @Inject()(
  trustedApp: CurrentApplication,
  ws: WSClient,
  cache: AsyncCacheApi,
  config: Configuration
)(implicit @Named("tileData") ec: ExecutionContext) extends TileContentService with Logging {

  import TileContentService._

  // TODO inject a client properly
  val client: CloseableHttpClient = HttpClientBuilder
    .create()
    .setMaxConnTotal(250)
    .setMaxConnPerRoute(100)
    .build()

  val preferenceCacheDuration: FiniteDuration = config
    .get[Int]("mywarwick.cache.tile-preferences.seconds").seconds

  override def getTilesOptions(user: Option[User], tiles: Seq[Tile]): Future[JsValue] = {
    getCachedTilesOptions(user, tiles).map(JsObject.apply)
  }

  private def signWSRequest(application: CurrentApplication, user: Option[User], req: WSRequest): WSRequest =
    user.map { u =>
      val certificate: EncryptedCertificate = application.encode(u.usercode.string, req.uri.toString)
      req.addHttpHeaders(
        (TrustedApplication.HEADER_PROVIDER_ID, certificate.getProviderID),
        (TrustedApplication.HEADER_CERTIFICATE, certificate.getCertificate),
        (TrustedApplication.HEADER_SIGNATURE, certificate.getSignature)
      )
    }.getOrElse(req)

  def getCachedTilesOptions(user: Option[User], tiles: Seq[Tile]): Future[Seq[(String, JsValue)]] = {
    Future.sequence(tiles.map { tile =>
      tile.fetchUrl.map { url =>
        cache.getOrElseUpdate(s"tile-preferences-${tile.id}", preferenceCacheDuration) {
          signWSRequest(trustedApp, user, ws.url(s"$url/preferences.json")).get()
            .map(res =>
              res.status match {
                case 200 => (tile.id, res.json)
                case 404 => (tile.id, Json.obj())
                case _ =>
                  logger.error(s"Error requesting preferences for tile ${tile.id}, res: $res")
                  (tile.id, Json.obj())
              }
            )
            .recover {
              case e =>
                logger.error(s"Error requesting preferences for tile ${tile.id}", e)
                (tile.id, Json.obj())
            }
        }
      }.getOrElse(Future.successful((tile.id, Json.obj())))
    })
  }

  // TODO cache
  override def getTileContent(usercode: Option[Usercode], tileInstance: TileInstance): Future[API.Response[JsObject]] =
    tileInstance.tile.fetchUrl.map { fetchUrl =>
      Future {
        val request = jsonPost(fetchUrl, tileInstance.preferences)

        usercode.foreach(usercode => signRequest(trustedApp, usercode.string, request))

        var response: CloseableHttpResponse = null

        val serviceName = tileInstance.tile.title.toLowerCase

        val result = Try {
          request.setConfig(getRequestConfigForTile(tileInstance))
          response = client.execute(request)
          val body = CharStreams.toString(new InputStreamReader(response.getEntity.getContent, Charsets.UTF_8))
          val apiResponse = Json.parse(body).as[API.Response[JsObject]]

          if (!apiResponse.success) {
            logger.warn(s"Content provider returned failure: user=${usercode.map(_.string).getOrElse("anonymous")}, tile=${tileInstance.tile.id}, response=$body")
          }

          apiResponse
        }.recover {
          case e =>
            logger.warn(s"Error fetching tile content: user=${usercode.map(_.string).getOrElse("anonymous")}, tile=${tileInstance.tile.id}", e)
            throw e
        }.recover {
          case _: JsonProcessingException | _: JsResultException => error('parse, s"The $serviceName service returned an unexpected response")
          case _: HttpHostConnectException => error('io, s"Couldn't connect to the $serviceName service")
          case _: SocketTimeoutException => error('timeout, s"The $serviceName service isn't responding right now")
          case _: IOException => error('io, s"Couldn't read from the $serviceName service")
          case _ => error('unknown, "An error occurred")
        }

        if (response != null) response.close()

        result.get
      }
    }.getOrElse {
      Future.failed(new IllegalArgumentException(s"Tile type ${tileInstance.tile.id} does not have a fetch URL"))
    }

  private def error(kind: Symbol, message: String): API.Failure[JsObject] = {
    API.Failure("error", Seq(API.Error(kind.name, message)))
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
