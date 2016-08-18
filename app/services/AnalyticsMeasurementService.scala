package services

import java.util.UUID

import com.google.inject.{ImplementedBy, Inject, Singleton}
import models.Hit
import org.apache.commons.codec.digest.DigestUtils
import play.api.Configuration
import play.api.libs.ws.WSAPI
import play.api.mvc.RequestHeader
import system.RequestContext
import warwick.sso.Usercode

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class AnalyticsTrackingID(string: String)

@ImplementedBy(classOf[AnalyticsMeasurementServiceImpl])
trait AnalyticsMeasurementService {
  def tracker(
    clientID: String,
    ip: Option[String] = None,
    userAgent: Option[String] = None,
    userID: Option[String] = None
  ): AnalyticsTracker

  def tracker(implicit request: RequestHeader, context: RequestContext): AnalyticsTracker

  def getUserIdentifier(usercode: Usercode): String

  val trackingID: AnalyticsTrackingID
}

trait AnalyticsTracker {
  def send(hit: Hit): Future[Unit]
}

@Singleton
class AnalyticsMeasurementServiceImpl @Inject()(
  ws: WSAPI,
  configuration: Configuration
) extends AnalyticsMeasurementService {

  val trackingID = configuration.getString("start.analytics.tracking-id").map(AnalyticsTrackingID)
    .getOrElse(throw new IllegalStateException("Analytics tracking ID missing - check start.analytics.tracking-id"))

  val identifierSalt = configuration.getString("start.analytics.identifier.salt")
    .getOrElse(throw new IllegalStateException("Analytics identifier salt missing - check start.analytics.identifier.salt"))

  override def getUserIdentifier(usercode: Usercode) =
    DigestUtils.sha256Hex(identifierSalt + usercode.string)

  override def tracker(implicit request: RequestHeader, context: RequestContext) =
    tracker(
      clientID = request.getQueryString("clientId").getOrElse(UUID.randomUUID().toString),
      ip = Option(request.remoteAddress),
      userAgent = request.headers.get("User-Agent"),
      userID = context.user.map(_.usercode).map(getUserIdentifier)
    )

  override def tracker(
    clientID: String,
    ip: Option[String],
    userAgent: Option[String],
    userID: Option[String]
  ): AnalyticsTracker =
    TrackerImpl(trackingID, clientID, ip, userAgent, userID)

  case class TrackerImpl(
    trackingID: AnalyticsTrackingID,
    clientID: String,
    ip: Option[String],
    userAgent: Option[String],
    userID: Option[String]
  ) extends AnalyticsTracker {
    val trackerAttributes = Map(
      "v" -> Seq("1"),
      "tid" -> Seq(trackingID.string),
      "cid" -> Seq(clientID),
      "uip" -> ip.toSeq,
      "ua" -> userAgent.toSeq,
      "uid" -> userID.toSeq
    )

    def send(hit: Hit): Future[Unit] = {
      val body = trackerAttributes ++ hit.attributes

      ws.url("https://www.google-analytics.com/collect")
        .post(body)
        .map(_ => ())
    }
  }

}
