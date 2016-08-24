package controllers.api

import javax.inject.Singleton

import com.google.inject.Inject
import controllers.BaseController
import models.{API, EventHit}
import org.joda.time.DateTime
import play.api.libs.json._
import services.{AnalyticsMeasurementService, NewsService, SecurityService}

@Singleton
class ReadNewsController @Inject()(
  news: NewsService,
  security: SecurityService,
  measurementService: AnalyticsMeasurementService
) extends BaseController {

  import security._

  def feed = UserAction { request =>
    val offset = request.getQueryString("offset").map(Integer.parseInt).getOrElse(0)

    val userNews = news.latestNews(request.context.user.map(_.usercode), limit = 10, offset = offset)

    Ok(Json.toJson(API.Success(data = userNews)))
  }

  def redirect(id: String) = UserAction { implicit request =>
    news.getNewsItem(id).flatMap(_.link).map { link =>
      measurementService.tracker.send(EventHit(
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
