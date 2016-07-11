package controllers.api

import javax.inject.Singleton

import com.google.inject.Inject
import controllers.BaseController
import org.joda.time.DateTime
import play.api.libs.json._
import services.{NewsService, SecurityService}

@Singleton
class ReadNewsController @Inject()(
  news: NewsService,
  security: SecurityService
) extends BaseController {

  import security._

  def feed = UserAction { implicit request =>
    val userNews = news.latestNews(request.context.user.map(_.usercode), limit = 100)

    Ok(Json.obj(
      "items" -> userNews
    ))
  }

}
