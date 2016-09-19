package controllers.api

import com.google.inject.Inject
import controllers.BaseController
import play.api.libs.json.Json
import play.api.mvc.Action
import services.analytics.{AnalyticsReport, NewsAnalyticsService}

class AnalyticsController @Inject()(
  news: NewsAnalyticsService
) extends BaseController {

  def newsItemReport = Action { implicit request =>
    request.body.asJson.map { json =>
      val ids = (json \ "ids").as[Seq[String]]
      Ok(Json.toJson(
        Map(news.getClicks(ids).map {
          case AnalyticsReport(i, m) => i -> m
        }: _*)
      ))
    }.getOrElse(BadRequest)
  }
}
