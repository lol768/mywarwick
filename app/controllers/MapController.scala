package controllers

import javax.inject.{Inject, Singleton}

import akka.stream.Materializer
import org.joda.time.DateTime
import play.api.http.HttpEntity
import play.api.libs.ws.{StreamedResponse, WSResponseHeaders}
import play.api.mvc.{Action, AnyContent}
import services.MapService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class MapController @Inject()(
  implicit val mat: Materializer,
  mapService: MapService
) extends BaseController {

  private val PUBLIC_MAX_AGE_ONE_WEEK = "public, max-age: 604800"

  private def isOk(resp: WSResponseHeaders) = resp.status == OK

  private def isContentType(resp: WSResponseHeaders, prefix: String) = getContentType(resp).startsWith(prefix)

  private def getContentType(resp: WSResponseHeaders) = resp.headers.get("Content-Type").flatMap(_.headOption).getOrElse("application/octet-stream")

  def mapThumbnail(lon: String, lat: String, width: Int, height: Int): Action[AnyContent] = Action.async { implicit request =>
    val now = DateTime.now().toString

    mapService.thumbnailForLocation(lon, lat, width, height).flatMap {
      case StreamedResponse(response, body) if isOk(response) && isContentType(response, "image") =>
        val contentType = getContentType(response)

        response.headers.get("Content-Length") match {
          case Some(Seq("0")) =>
            val err = "Empty response from remote map service"
            logger.error(err)
            Future.successful(BadGateway(s"$err ($now)"))
          case Some(Seq(length)) =>
            Future.successful(
              Ok.sendEntity(HttpEntity.Streamed(body, Some(length.toLong), Some(contentType)))
                .withHeaders(CACHE_CONTROL -> PUBLIC_MAX_AGE_ONE_WEEK)
            )
          case _ =>
            Future.successful(
              Ok.chunked(body).as(contentType)
                .withHeaders(CACHE_CONTROL -> PUBLIC_MAX_AGE_ONE_WEEK)
            )
        }
      case StreamedResponse(response, body) if isOk(response) && isContentType(response, "text") =>
        body
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
