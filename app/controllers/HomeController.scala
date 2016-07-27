package controllers

import javax.inject.{Inject, Singleton}

import play.api.Configuration
import play.api.mvc._
import services.{PhotoService, SecurityService}
import system.AppMetrics
import system.ThreadPools.externalData

import scala.concurrent.Future

case class AnalyticsTrackingID(string: String)

case class SearchRootUrl(string: String)

@Singleton
class HomeController @Inject()(
  security: SecurityService,
  metrics: AppMetrics,
  photoService: PhotoService,
  configuration: Configuration
) extends BaseController {

  import security._

  implicit val analyticsTrackingId: Option[AnalyticsTrackingID] =
    configuration.getString("start.analytics.tracking-id").map(AnalyticsTrackingID)

  implicit val searchRootUrl: SearchRootUrl =
    configuration.getString("start.search.root").map(SearchRootUrl)
      .getOrElse(throw new IllegalStateException("Search root URL not configured - check start.search.root property"))

  def index = Action(Ok(views.html.index()))

  def redirectToIndex = Action(Redirect(routes.HomeController.index()))

  def tile(id: String) = index

  def photo = RequiredUserAction.async { request =>
    photoService.photoUrl(request.context.user.flatMap(_.universityId)).map(url => Redirect(url))
  }

  def redirectToPath(path: String, status: Int = MOVED_PERMANENTLY) = Action { implicit request =>
    Redirect(s"/${path.replaceFirst("^/", "")}", status)
  }
}
