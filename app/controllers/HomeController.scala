package controllers

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.mvc._
import services.analytics.{AnalyticsMeasurementService, AnalyticsTrackingID}
import system.AppMetrics

import scala.concurrent.ExecutionContext

case class SearchRootUrl(string: String)

@Singleton
class HomeController @Inject()(
  metrics: AppMetrics,
  configuration: Configuration,
  measurementService: AnalyticsMeasurementService,
  assets: Assets
)(implicit executionContext: ExecutionContext) extends MyController {

  implicit val analyticsTrackingId: Option[AnalyticsTrackingID] = Some(measurementService.trackingID)

  implicit val searchRootUrl: SearchRootUrl =
    configuration.getOptional[String]("mywarwick.search.root").map(SearchRootUrl)
      .getOrElse(throw new IllegalStateException("Search root URL not configured - check mywarwick.search.root property"))

  implicit val vapidPublicKey: String = configuration.get[String]("mywarwick.vapid.publicKey")

  def index = Action { implicit request =>
    Ok(views.html.index())
  }

  def settings: Action[AnyContent] = index

  def redirectToIndex = Action(Redirect(routes.HomeController.index()))

  def indexIgnoreParam(param: String): Action[AnyContent] = index

  def redirectToPath(path: String, status: Int = MOVED_PERMANENTLY) = Action { implicit request =>
    Redirect(s"/${path.replaceFirst("^/", "")}", status)
  }

  def serviceWorker: Action[AnyContent] = Action.async(parse.default) { implicit request =>
    assets.at(path="/public", file="service-worker.js")(request)
      .map(_.withHeaders("Expires" -> "0"))
  }
}
