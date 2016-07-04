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

  def feed = UserAction { implicit req =>
    val userNews = news.latestNews(req.context.user.map(_.usercode))

    Ok(Json.obj(
      "items" -> userNews
    ))
  }

}
