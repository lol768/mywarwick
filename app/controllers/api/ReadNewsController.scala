package controllers.api

import javax.inject.Singleton

import com.google.inject.Inject
import controllers.BaseController
import models.API
import play.api.libs.json._
import services.{NewsService, SecurityService}

@Singleton
class ReadNewsController @Inject()(
  news: NewsService,
  security: SecurityService
) extends BaseController {

  import security._

  def feed = UserAction { request =>
    val userNews = news.latestNews(request.context.user.map(_.usercode))

    Ok(Json.toJson(API.Success(data = userNews)))
  }

}
