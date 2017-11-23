package services

import javax.inject.{Inject, Singleton}

import play.api.Configuration
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.libs.ws.ahc.StreamedResponse

import scala.concurrent.Future

@Singleton
class MapService @Inject()(
  ws: WSClient,
  configuration: Configuration
) {

  val conf = configuration.get[Configuration]("webservice.map.thumbnail")

  val url: String = conf.get[String]("urlPath")
  val query: Seq[(String, String)] = conf.get[Option[Seq[String]]]("urlParams").getOrElse(Nil)
    .map(_.split("=", 2))
    .map { case Array(key, value) => (key, value) }

  def thumbnailForLocation(lat: String, lon: String, width: Int, height: Int): Future[WSResponse] = {
    ws.url(url).withMethod("GET")
      .addQueryStringParameters(query: _*)
      .addQueryStringParameters(
        "gps" -> s"$lat,$lon",
        "crop" -> s"${width}x$height"
      )
      .stream
  }

}
