package controllers

import javax.inject.{Inject, Singleton}

import akka.stream.Materializer
import org.joda.time.DateTime
import play.api.http.HttpEntity
import play.api.libs.ws.WSResponse
import play.api.mvc.{Action, AnyContent}
import play.api.libs.ws.ahc._
import services.MapService
import system.ThreadPools.externalData

import scala.collection.JavaConverters._
import scala.concurrent.Future

@Singleton
class MapController @Inject()(
  implicit val mat: Materializer,
  mapService: MapService
) extends MyController {

  private val PUBLIC_MAX_AGE_ONE_WEEK = "public, max-age: 604800"

  private def isOk(resp: WSResponse) = resp.status == OK

  private def isContentType(resp: WSResponse, prefix: String) = getContentType(resp).startsWith(prefix)

  private def getContentType(resp: WSResponse) = resp.headers.get("Content-Type").flatMap(_.headOption).getOrElse("application/octet-stream")

  def mapThumbnail(lat: String, lon: String, width: Int, height: Int): Action[AnyContent] = Action.async { implicit request =>
    val now = DateTime.now().toString

    mapService.thumbnailForLocation(lat, lon, width, height).flatMap {
      case res if isOk(res) && isContentType(res, "image") =>
        val contentType = getContentType(res)

        res.headers.get("Content-Length") match {
          case Some(Seq("0")) =>
            val err = "Empty response from remote map service"
            logger.error(err)
            Future.successful(BadGateway(s"$err ($now)"))
          case Some(Seq(length)) =>
            Future.successful(
              Ok.sendEntity(HttpEntity.Streamed(res.bodyAsSource, Some(length.toLong), Some(contentType)))
                .withHeaders(CACHE_CONTROL -> PUBLIC_MAX_AGE_ONE_WEEK)
            )
          case _ =>
            Future.successful(
              Ok.chunked(res.bodyAsSource).as(contentType)
                .withHeaders(CACHE_CONTROL -> PUBLIC_MAX_AGE_ONE_WEEK)
            )
        }
      case res if isOk(res) && isContentType(res, "text") =>
        res.bodyAsSource
          .runReduce(_ ++ _)
          .map(_.utf8String)
          .collect {
            case s: String if s.toLowerCase.contains("not found") =>
              val err = "Map location not found"
              logger.error(err)
              NotFound(s"$err ($now)")
            case s: String =>
              logger.error(s"Unexpected error from remote map service, ${s.take(150)}")
              BadGateway(s"Unexpected error from remote map service ($now)")
          }
      case _ =>
        val err = s"Unexpected response from remote map service, for location $lon,$lat"
        logger.error(err)
        Future.successful(BadGateway(s"$err ($now)"))
    }
  }

}
