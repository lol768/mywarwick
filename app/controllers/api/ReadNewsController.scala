package controllers.api

import javax.inject.Singleton

import com.google.inject.Inject
import controllers.{AnalyticsTrackingID, BaseController}
import models.{API, EventHit}
import play.api.Configuration
import play.api.libs.json._
import services.{AnalyticsMeasurementService, NewsService, SecurityService}

@Singleton
class ReadNewsController @Inject()(
  news: NewsService,
  security: SecurityService,
  configuration: Configuration,
  measurementService: AnalyticsMeasurementService
) extends BaseController {

  import security._

  val analyticsTrackingId =
    configuration.getString("start.analytics.tracking-id").map(AnalyticsTrackingID)
      .getOrElse(throw new IllegalStateException("Analytics tracking ID must be configured - check start.analytics.tracking-id"))

  def feed = UserAction { request =>
    val userNews = news.latestNews(request.context.user.map(_.usercode), limit = 100)

    Ok(Json.toJson(API.Success(data = userNews)))
  }

  def redirect(id: String) = UserAction { implicit request =>
    news.getNewsItem(id).flatMap(_.link).map { link =>
      measurementService.tracker(analyticsTrackingId).send(EventHit(
        category = "News",
        action = "Click",
        label = Some(id)
      ))

      Redirect(link.href.toString)
    }.getOrElse {
      Redirect(controllers.routes.HomeController.index())
    }
  }

}
