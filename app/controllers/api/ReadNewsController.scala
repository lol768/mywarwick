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
    val userNews = req.context.user.map { user =>
      news.latestNews(user.usercode)
    }.getOrElse(Nil)

    val sortedNews = (userNews).sortBy(_.publishDate)
    Ok(Json.obj(
      "items" -> sortedNews
    ))
  }

  implicit def newestFirst: Ordering[DateTime] = Ordering.fromLessThan(_ isAfter _)

}
