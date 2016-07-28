package controllers

import javax.inject.{Inject, Singleton}

import play.api.Configuration
import play.api.mvc._
import system.AppMetrics

case class AnalyticsTrackingID(string: String)

case class SearchRootUrl(string: String)

@Singleton
class HomeController @Inject()(
  metrics: AppMetrics,
  configuration: Configuration
) extends BaseController {

  implicit val analyticsTrackingId: Option[AnalyticsTrackingID] =
    configuration.getString("start.analytics.tracking-id").map(AnalyticsTrackingID)

  implicit val searchRootUrl: SearchRootUrl =
    configuration.getString("start.search.root").map(SearchRootUrl)
      .getOrElse(throw new IllegalStateException("Search root URL not configured - check start.search.root property"))

  def index = Action(Ok(views.html.index()))

  def redirectToIndex = Action(Redirect(routes.HomeController.index()))

  def tile(id: String) = index

  def redirectToPath(path: String, status: Int = MOVED_PERMANENTLY) = Action { implicit request =>
    Redirect(s"/${path.replaceFirst("^/", "")}", status)
  }
}
