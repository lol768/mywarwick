package controllers.api

import com.google.inject.Inject
import controllers.BaseController
import play.api.libs.json.Json
import play.api.mvc.Action
import services.analytics.NewsAnalyticsService

class AnalyticsController @Inject()(
  news: NewsAnalyticsService
) extends BaseController {

  def newsItemReport = Action { implicit request =>
    request.body.asJson.map { json =>
      val ids = (json \ "ids").as[Seq[String]]
      Ok(Json.obj(
        "clicks" -> news.getClicks(ids).map(_.metric)
      ))
    }.getOrElse(BadRequest)
  }
}
