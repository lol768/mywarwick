package controllers.api

import com.google.inject.Inject
import controllers.BaseController
import play.api.libs.json.Json
import play.api.mvc.Action
import services.analytics.{CombinedRow, NewsAnalyticsService}

class AnalyticsController @Inject()(
  news: NewsAnalyticsService
) extends BaseController {

  def newsItemReport = Action { implicit request =>
    request.body.asJson.map { json =>
      val ids = (json \ "ids").as[Seq[String]]
      Ok(Json.toJson(
        news.getClicks(ids).map { row =>
          row.id -> Json.obj(
            "guests" -> row.guests,
            "users" -> row.users
          )
        }.toMap
      ))
    }.getOrElse(BadRequest)
  }
}
