package controllers.api

import javax.inject.Singleton

import com.google.inject.Inject
import com.sun.syndication.feed.synd._
import com.sun.syndication.io.WireFeedOutput
import controllers.BaseController
import models.{API, PageViewHit}
import org.jdom.{Element, Namespace}
import play.api.libs.json._
import play.api.mvc.RequestHeader
import services.analytics.AnalyticsMeasurementService
import services.{NewsService, SecurityService}
import warwick.sso.User

import scala.collection.JavaConverters._

@Singleton
class ReadNewsController @Inject()(
  news: NewsService,
  security: SecurityService,
  measurementService: AnalyticsMeasurementService
) extends BaseController {

  private val output = new WireFeedOutput()

  import security._

  def feed(offset: Int) = UserAction { request =>
    val userNews = news.latestNews(request.context.user.map(_.usercode), limit = 10, offset = offset)

    Ok(Json.toJson(API.Success(data = userNews)))
  }

  def rssFeed = UserAction { implicit request =>
    val feed = buildFeedForUser(request.context.user)

    Ok(writeFeedString(feed, "rss_2.0")).as("application/rss+xml")
  }

  def atomFeed = UserAction { implicit request =>
    val feed = buildFeedForUser(request.context.user)

    Ok(writeFeedString(feed, "atom_1.0")).as("application/atom+xml")
  }

  def redirect(id: String) = UserAction { implicit request =>
    (for {
      item <- news.getNewsItem(id)
      link <- item.link
    } yield {
      measurementService.tracker.send(PageViewHit(
        url = controllers.api.routes.ReadNewsController.redirect(id).absoluteURL(),
        title = Some(item.title)
      ))

      Redirect(link.href.toString)
    }).getOrElse(
      Redirect(controllers.routes.HomeController.index())
    )
  }

  private def buildFeedForUser(user: Option[User])(implicit request: RequestHeader) = {
    val userNews = news.latestNews(user.map(_.usercode), limit = 20)

    val feed = new SyndFeedImpl()
    feed.setTitle("News for " + user.flatMap(_.name.first).getOrElse("everyone"))
    feed.setDescription(if (user.isEmpty) "Public news" else "News for you")
    feed.setAuthor("University of Warwick")
    feed.setLink("https://my.warwick.ac.uk")

    val entries = userNews.map { item =>
      val entry: SyndEntry = new SyndEntryImpl()

      val content = new SyndContentImpl
      content.setType("text/plain")
      content.setValue(item.text)

      entry.setTitle(item.title)
      entry.setPublishedDate(item.publishDate.toDate)
      entry.setDescription(content)
      item.link.map(_.href.toString).foreach(entry.setLink)

      entry.setCategories(item.categories.map { cat =>
        val impl: SyndCategory = new SyndCategoryImpl()
        impl.setName(cat.name)
        impl
      }.asJava)

      item.imageId.foreach { imageId =>
        val el = new Element("image", Namespace.getNamespace("image", "http://web.resource.org/rss/1.0/modules/image/"))
        el.addContent(controllers.api.routes.NewsImagesController.show(imageId).absoluteURL)
        entry.setForeignMarkup(Seq(el).asJava)
      }

      entry
    }.asJava

    feed.setEntries(entries)
    feed
  }

  private def writeFeedString(feed: SyndFeed, format: String) = {
    val wireFeed = feed.createWireFeed(format)

    output.outputString(wireFeed)
  }

}
