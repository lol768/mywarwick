package services

import java.util.UUID

import com.google.inject.{ImplementedBy, Inject, Singleton}
import controllers.AnalyticsTrackingID
import models.Hit
import play.api.libs.ws.WSAPI
import play.api.mvc.RequestHeader

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@ImplementedBy(classOf[AnalyticsMeasurementServiceImpl])
trait AnalyticsMeasurementService {
  def tracker(
    trackingID: AnalyticsTrackingID,
    clientID: String,
    ip: Option[String] = None,
    userAgent: Option[String] = None
  ): AnalyticsTracker

  def tracker(trackingID: AnalyticsTrackingID)(implicit request: RequestHeader): AnalyticsTracker
}

trait AnalyticsTracker {
  def send(hit: Hit): Future[Unit]
}

@Singleton
class AnalyticsMeasurementServiceImpl @Inject()(
  ws: WSAPI
) extends AnalyticsMeasurementService {

  override def tracker(trackingID: AnalyticsTrackingID)(implicit request: RequestHeader) =
    tracker(
      trackingID = trackingID,
      clientID = request.getQueryString("clientId").getOrElse(UUID.randomUUID().toString),
      ip = Option(request.remoteAddress),
      userAgent = request.headers.get("User-Agent")
    )

  override def tracker(
    trackingID: AnalyticsTrackingID,
    clientID: String,
    ip: Option[String],
    userAgent: Option[String]
  ): AnalyticsTracker =
    TrackerImpl(trackingID, clientID, ip, userAgent)

  case class TrackerImpl(
    trackingID: AnalyticsTrackingID,
    clientID: String,
    ip: Option[String],
    userAgent: Option[String]
  ) extends AnalyticsTracker {
    val trackerAttributes = Map(
      "v" -> Seq("1"),
      "tid" -> Seq(trackingID.string),
      "cid" -> Seq(clientID),
      "uip" -> ip.toSeq,
      "ua" -> userAgent.toSeq
    )

    def send(hit: Hit): Future[Unit] = {
      val body = trackerAttributes ++ hit.attributes

      ws.url("https://www.google-analytics.com/collect")
        .post(body)
        .map(_ => ())
    }
  }

}
