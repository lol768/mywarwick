package services

import javax.inject.{Inject, Singleton}

import play.api.Configuration
import play.api.libs.ws.{StreamedResponse, WSClient}

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

  def thumbnailForLocation(lon: String, lat: String, width: Int, height: Int): Future[StreamedResponse] = {
    ws.url(url).withMethod("GET")
      .withQueryString(query: _*)
      .withQueryString(
        "gps" -> s"$lon,$lat",
        "crop" -> s"${width}x$height"
      )
      .stream
  }

}
