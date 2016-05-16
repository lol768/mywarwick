package controllers.api

import javax.inject.Singleton

import com.google.inject.Inject
import controllers.BaseController
import models.{NewsItem, NewsSource}
import org.joda.time.DateTime
import play.api.db.Database
import play.api.libs.json._
import play.api.mvc.Action
import services.dao.NewsDao
import services.{FeedService, NewsService, SecurityService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class ReadNewsController @Inject()(
  newsService: NewsService,
  feedService: FeedService,
  newsDao: NewsDao,
  security: SecurityService,
  db: Database
) extends BaseController {

  import security._

  def feed = RequiredUserAction.async { implicit req =>
    val futures = newsService.allSources
      .map(source => feedService.fetch(source).map(feed => (source, feed)))

    // FIXME tacked on to existing news source stuff - needs refactoring
    val userSource = NewsSource("Personalised news", null, "gold")
    val userNews = req.context.user.map { user =>
      db.withConnection { implicit c =>
        newsDao.latestNews(user.usercode)
      }
    }.getOrElse(Nil)

    Future.sequence(futures).map { results =>
      val items = results.flatMap { case (source, feed) => feed.items.map(_.asNewsItem(source)) }
      val userItems = userNews.map { item =>
        NewsItem(
          source = userSource,
          id = item.id,
          title = item.title,
          url = item.link.map(_.href.toString),
          content = item.text,
          publicationDate = item.publishDate
        )
      }
      val allItems = (items ++ userItems).sortBy(_.publicationDate)
      Ok(Json.obj("items" -> allItems))
    }
  }

  implicit def newestFirst: Ordering[DateTime] = Ordering.fromLessThan(_ isAfter _)

}
