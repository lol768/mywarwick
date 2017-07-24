package controllers

import javax.inject.{Inject, Singleton}

import play.api.Configuration
import play.api.mvc._
import services.{SecurityService, UserPreferencesService}
import services.analytics.{AnalyticsMeasurementService, AnalyticsTrackingID}
import system.AppMetrics

import scala.concurrent.Future

case class SearchRootUrl(string: String)

@Singleton
class HomeController @Inject()(
  securityService: SecurityService,
  metrics: AppMetrics,
  configuration: Configuration,
  measurementService: AnalyticsMeasurementService,
  prefsService: UserPreferencesService
) extends BaseController {

  import securityService._


  implicit val analyticsTrackingId: Option[AnalyticsTrackingID] = Some(measurementService.trackingID)

  implicit val searchRootUrl: SearchRootUrl =
    configuration.getString("mywarwick.search.root").map(SearchRootUrl)
      .getOrElse(throw new IllegalStateException("Search root URL not configured - check mywarwick.search.root property"))

  implicit val showBetaWarning: Boolean =
    configuration.getBoolean("mywarwick.showBetaWarning").getOrElse(false)

  def index = UserAction { request =>

    val headers = List(request.context.user.map(
      u => "Link" ->
        "<%s>; rel=preload; as=image".format(
          routes.Assets.versioned("images/bg%02d.jpg".format(prefsService.getChosenColourScheme(u.usercode))))
    )).flatten

    Ok(views.html.index()).withHeaders(headers:_*)
  }

  def settings: Action[AnyContent] = index

  def redirectToIndex = Action(Redirect(routes.HomeController.index()))

  def indexIgnoreParam(param: String): Action[AnyContent] = index

  def redirectToPath(path: String, status: Int = MOVED_PERMANENTLY) = Action { implicit request =>
    Redirect(s"/${path.replaceFirst("^/", "")}", status)
  }
}
