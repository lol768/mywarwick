package controllers

import com.google.inject.Inject
import models.NewsItem
import play.api.libs.json._
import play.api.mvc.{Action, Controller}
import services.{FeedService, NewsService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class NewsController @Inject()(
  newsService: NewsService,
  feedService: FeedService
) extends Controller {

  def feed = Action.async {
    val futures = newsService.allSources
      .map(source => feedService.fetch(source).map(feed => (source, feed)))

    Future.sequence(futures).map { results =>
      val items = results.flatMap { case (source, feed) => feed.items.map(_.asNewsItem(source)) }

      Ok(Json.obj("items" -> items))
    }
  }

}
