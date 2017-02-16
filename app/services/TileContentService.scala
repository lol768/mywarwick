package services

import java.io.{IOException, InputStreamReader}
import java.net.SocketTimeoutException
import javax.inject.Inject

import com.fasterxml.jackson.core.JsonProcessingException
import com.google.common.base.Charsets
import com.google.common.io.CharStreams
import com.google.inject.ImplementedBy
import models.{API, Tile, TileInstance}
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods._
import org.apache.http.conn.HttpHostConnectException
import org.apache.http.entity.{ContentType, StringEntity}
import org.apache.http.impl.client.HttpClientBuilder
import play.api.cache._
import play.api.libs.json.{JsObject, _}
import play.api.libs.ws.WSClient
import system.{Logging, ThreadPools}
import uk.ac.warwick.sso.client.trusted.{CurrentApplication, TrustedApplicationUtils}
import warwick.sso.User

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Try

object TileContentService {

  val connectTimeout = 5.seconds
  val socketTimeout = 5.seconds

}

@ImplementedBy(classOf[TileContentServiceImpl])
trait TileContentService {

  def getTilesOptions(tiles: Seq[Tile]): Future[JsValue]

  def getTileContent(user: Option[User], tileInstance: TileInstance): Future[API.Response[JsObject]]

}

class TileContentServiceImpl @Inject()(
  trustedApp: CurrentApplication,
  ws: WSClient,
  cache: CacheApi
) extends TileContentService with Logging {

  import TileContentService._

  val requestConfig = RequestConfig.custom()
    .setConnectTimeout(connectTimeout.toMillis.toInt)
    .setSocketTimeout(socketTimeout.toMillis.toInt)
    .build()

  // TODO inject a client properly
  val client = HttpClientBuilder.create()
    .setDefaultRequestConfig(requestConfig)
    .build()

  import ThreadPools.tileData

  override def getTilesOptions(tiles: Seq[Tile]): Future[JsValue] = {
    val allOptions: Seq[Future[(String, JsValue)]] = tiles.map { tile =>
      tile.fetchUrl match {
        case Some(url) => {
          ws.url(s"${url}/preferences.json")
            .get()
            .map(res =>
              res.status match {
                case 200 => (tile.id, res.json)
                case 404 => (tile.id, Json.obj())
                case _ =>
                  logger.error(s"error requesting preferences for a tile, res: ${res}")
                  (tile.id, Json.obj())
              }
            )
        }
        case None => Future.successful((tile.id, Json.obj()))
      }
    }
    Future.sequence(allOptions).map(JsObject.apply)
  }

  // TODO cache
  override def getTileContent(user: Option[User], tileInstance: TileInstance): Future[API.Response[JsObject]] =
    tileInstance.tile.fetchUrl.map { fetchUrl =>
      Future {
        val request = jsonPost(fetchUrl, tileInstance.preferences)
        user.foreach(user => signRequest(trustedApp, user.usercode.string, request))

        var response: CloseableHttpResponse = null

        val serviceName = tileInstance.tile.title.toLowerCase

        val result = Try {
          response = client.execute(request)
          val body = CharStreams.toString(new InputStreamReader(response.getEntity.getContent, Charsets.UTF_8))
          val apiResponse = Json.parse(body).as[API.Response[JsObject]]

          if (!apiResponse.success) {
            logger.warn(s"Content provider returned failure: user=${user.map(_.usercode.string).getOrElse("anonymous")}, tile=${tileInstance.tile.id}, response=$body")
          }

          apiResponse
        }.recover {
          case e =>
            logger.warn(s"Error fetching tile content: user=${user.map(_.usercode.string).getOrElse("anonymous")}, tile=${tileInstance.tile.id}", e)
            throw e
        }.recover {
          case _: JsonProcessingException | _: JsResultException => error('parse, s"The $serviceName service returned an unexpected response.")
          case _: HttpHostConnectException => error('io, s"Couldn't connect to the $serviceName service.")
          case _: SocketTimeoutException => error('timeout, s"The $serviceName service isn't responding right now.")
          case _: IOException => error('io, s"Couldn't read from the $serviceName service.")
          case _ => error('unknown, "An error occurred.")
        }

        if (response != null) response.close()

        result.get
      }
    }
      .getOrElse {
        Future.failed(new IllegalArgumentException(s"Tile type ${tileInstance.tile.id} does not have a fetch URL"))
      }

  private def error(kind: Symbol, message: String): API.Failure[JsObject] = {
    API.Failure("error", Seq(API.Error(kind.toString(), message)))
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
