package controllers

import javax.inject.Singleton

import com.google.inject.Inject
import models.NewsItem
import org.joda.time.DateTime
import play.api.libs.json._
import play.api.mvc.{Action, Controller}
import services.{FeedService, NewsService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class NewsController @Inject()(
  newsService: NewsService,
  feedService: FeedService
) extends BaseController {

  def feed = Action.async {
    val futures = newsService.allSources
      .map(source => feedService.fetch(source).map(feed => (source, feed)))

    Future.sequence(futures).map { results =>
      val items = results.flatMap { case (source, feed) => feed.items.map(_.asNewsItem(source)) }.sortBy(_.publicationDate)

      Ok(Json.obj("items" -> items))
    }
  }

  implicit def newestFirst: Ordering[DateTime] = Ordering.fromLessThan(_ isAfter _)

}
