package services

import javax.inject.{Inject, Singleton}

import play.api.Configuration
import play.api.libs.ws.WSClient
import play.api.libs.ws.ahc.StreamedResponse

import scala.concurrent.Future

@Singleton
class MapService @Inject()(
  ws: WSClient,
  configuration: Configuration
) {

  val url: String = configuration.getString("webservice.map.thumbnail.urlPath").getOrElse(throw new IllegalStateException("Missing Map web service thumbnail URL in config (webservice.map.thumbnail.urlPath)"))
  val query: Seq[(String, String)] = configuration.getStringSeq("webservice.map.thumbnail.urlParams").getOrElse(Seq())
    .map(_.split("=", 2))
    .map { case Array(key, value) => (key, value) }

  def thumbnailForLocation(lat: String, lon: String, width: Int, height: Int): Future[StreamedResponse] = {
    ws.url(url).withMethod("GET")
      .addQueryStringParameters(query: _*)
      .addQueryStringParameters(
        "gps" -> s"$lat,$lon",
        "crop" -> s"${width}x$height"
      )
      .stream
  }

}
