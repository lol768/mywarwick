package controllers.api

import javax.inject.Singleton

import com.google.inject.Inject
import com.sun.syndication.feed.synd._
import com.sun.syndication.io.WireFeedOutput
import controllers.BaseController
import models.Audience.DepartmentSubset
import models.news.NewsItemRender
import models.{API, PageViewHit}
import org.jdom.{Element, Namespace}
import play.api.libs.json._
import play.api.mvc.{Action, RequestHeader}
import services.analytics.AnalyticsMeasurementService
import services.{NewsService, SecurityService}
import warwick.sso.GroupName

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

  def rssFeed = Action { implicit request =>
    renderFeed("rss_2.0", "application/rss+xml")(request)
  }

  def atomFeed = Action { implicit request =>
    renderFeed("atom_1.0", "application/atom+xml")(request)
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

  private def renderFeed(feedFormat: String, contentType: String)(implicit request: RequestHeader) = {
    val deptCode = request.getQueryString("deptCode")
    val publisherId = request.getQueryString("publisher")
    val userType = request.getQueryString("userType").flatMap(DepartmentSubset.unapply)
    val webGroup = request.getQueryString("webGroup").map(GroupName)

    if (request.getQueryString("userType").nonEmpty && userType.isEmpty) {
      BadRequest("Invalid userType")
    } else if (webGroup.isEmpty && deptCode.isEmpty && userType.isEmpty && publisherId.isEmpty) {
      BadRequest("Must specify webGroup, deptCode, userType or publisherId")
    } else {
      val newsItems = news.getNewsItemsMatchingAudience(webGroup, deptCode, userType, publisherId, limit = 20)
      val feed = buildFeed("News", newsItems)

      Ok(writeFeedString(feed, feedFormat))
        .as(contentType)
        .withHeaders(CACHE_CONTROL -> "public, max-age=600")
    }
  }

  private def buildFeed(title: String, userNews: Seq[NewsItemRender])(implicit request: RequestHeader) = {
    val feed = new SyndFeedImpl()
    feed.setTitle(title)
    feed.setDescription(title)
    feed.setAuthor("University of Warwick")
    feed.setLink(controllers.routes.HomeController.index().absoluteURL)

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
