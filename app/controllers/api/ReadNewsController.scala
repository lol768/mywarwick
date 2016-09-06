package controllers.api

import javax.inject.Singleton

import com.google.inject.Inject
import controllers.BaseController
import models.{API, PageViewHit}
import play.api.libs.json._
import services.{AnalyticsMeasurementService, NewsService, SecurityService}

@Singleton
class ReadNewsController @Inject()(
  news: NewsService,
  security: SecurityService,
  measurementService: AnalyticsMeasurementService
) extends BaseController {

  import security._

  def feed(offset: Int) = UserAction { request =>
    val userNews = news.latestNews(request.context.user.map(_.usercode), limit = 10, offset = offset)

    Ok(Json.toJson(API.Success(data = userNews)))
  }

  def redirect(id: String) = UserAction { implicit request =>
    (for {
      item <- news.getNewsItem(id)
      link <- item.link
    } yield {
      measurementService.tracker.send(PageViewHit(
        url = controllers.api.routes.ReadNewsController.redirect(id).absoluteURL(),
        title = Some(item.title)
      ))

      Redirect(link.href.toString)
    }).getOrElse(
      Redirect(controllers.routes.HomeController.index())
    )
  }

}
