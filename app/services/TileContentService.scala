package services

import java.io.{IOException, InputStreamReader}
import java.net.SocketTimeoutException
import javax.inject.Inject

import com.fasterxml.jackson.core.{JsonParseException, JsonProcessingException}
import com.google.common.base.Charsets
import com.google.common.io.CharStreams
import com.google.inject.ImplementedBy
import models.{API, TileInstance}
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods._
import org.apache.http.conn.HttpHostConnectException
import org.apache.http.entity.{ContentType, StringEntity}
import org.apache.http.impl.client.HttpClientBuilder
import play.api.libs.json.{JsObject, _}
import system.{Logging, ThreadPools}
import uk.ac.warwick.sso.client.trusted.{CurrentApplication, TrustedApplicationUtils}
import warwick.sso.User

import scala.concurrent.duration._
import scala.concurrent.Future
import scala.util.Try

object TileContentService {

  val connectTimeout = 5.seconds
  val socketTimeout = 5.seconds

}

@ImplementedBy(classOf[TileContentServiceImpl])
trait TileContentService {

  def getTileContent(user: Option[User], tileInstance: TileInstance): Future[API.Response[JsObject]]

}

class TileContentServiceImpl @Inject()(
  trustedApp: CurrentApplication
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

  // TODO cache
  override def getTileContent(user: Option[User], tileInstance: TileInstance): Future[API.Response[JsObject]] = Future {
    val request = jsonPost(tileInstance.tile.fetchUrl, tileInstance.preferences)
    user.foreach(user => signRequest(trustedApp, user.usercode.string, request))

    var response: CloseableHttpResponse = null

    val serviceName = tileInstance.tile.title.toLowerCase

    val result = Try {
      response = client.execute(request)
      val body = CharStreams.toString(new InputStreamReader(response.getEntity.getContent, Charsets.UTF_8))
      val apiResponse = Json.parse(body).as[API.Response[JsObject]]

      if (!apiResponse.success) {
        throw new FailureResponseException(body)
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

  class FailureResponseException(body: String) extends Throwable(body)

}
