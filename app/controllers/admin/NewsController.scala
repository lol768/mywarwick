package controllers.admin

import javax.inject.{Inject, Singleton}

import controllers.BaseController
import models.DateFormats
import models.news.{Audience, Link, NewsItemRender, NewsItemSave}
import org.joda.time.LocalDateTime
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.{I18nSupport, MessagesApi}
import services.dao.DepartmentInfoDao
import services.{NewsService, SecurityService}
import system.{Roles, TimeZones, Validation}
import uk.ac.warwick.util.web.Uri

import scala.concurrent.Future

case class NewsUpdate(item: NewsItemData)

case class NewsItemData(
  title: String,
  text: String,
  linkText: Option[String],
  linkHref: Option[String],
  publishDate: LocalDateTime,
  imageId: Option[String]
) {
  def toSave = NewsItemSave(
    title = title,
    text = text,
    link = for {
      t <- linkText
      h <- linkHref
    } yield Link(t, Uri.parse(h)),
    // TODO test this gives expected results of TZ&DST
    publishDate = publishDate.toDateTime(TimeZones.LONDON),
    imageId = imageId
  )
}

@Singleton
class NewsController @Inject()(
  security: SecurityService,
  val messagesApi: MessagesApi,
  news: NewsService,
  val departmentInfoDao: DepartmentInfoDao,
  audienceBinder: AudienceBinder
) extends BaseController with I18nSupport with Publishing[NewsItemData] {

  import Roles._
  import security._

  val newsDataMapping = mapping(
    "title" -> nonEmptyText,
    "text" -> nonEmptyText,
    "linkText" -> optional(text),
    "linkHref" -> optional(text).verifying("Invalid URL format", Validation.url),
    "publishDate" -> DateFormats.dateTimeLocalMapping,
    "imageId" -> optional(text)
  )(NewsItemData.apply)(NewsItemData.unapply)

  val publishNewsForm = publishForm(newsDataMapping)

  val updateNewsForm = Form(mapping(
    "item" -> newsDataMapping)
  (NewsUpdate.apply)(NewsUpdate.unapply))

  def list = RequiredActualUserRoleAction(Sysadmin) {
    val theNews = news.allNews(limit = 100)
    val counts = news.countRecipients(theNews.map(_.id))
    val (newsPending, newsPublished) = partitionNews(theNews)
    Ok(views.html.admin.news.list(newsPending, newsPublished, counts))
  }

  def createForm = RequiredActualUserRoleAction(Sysadmin).async {
    for {
      dopts <- departmentOptions
    } yield {
      Ok(views.html.admin.news.createForm(publishNewsForm, dopts))
    }
  }

  def create = RequiredActualUserRoleAction(Sysadmin).async { implicit req =>
    departmentOptions.flatMap { dopts =>
      val bound = publishNewsForm.bindFromRequest
      bound.fold(
        errorForm => Future.successful(Ok(views.html.admin.news.createForm(errorForm, dopts))),
        data => audienceBinder.bindAudience(data).map {
          case Left(errors) =>
            val errorForm = addFormErrors(bound, errors)
            Ok(views.html.admin.news.createForm(errorForm, dopts))
          case Right(audience) =>
            handleForm(data, audience)
        }
      )
    }
  }

  def handleForm(data: Publish[NewsItemData], audience: Audience) = {
    val newsItem = data.item.toSave
    news.save(newsItem, audience)
    Redirect(controllers.admin.routes.NewsController.list()).flashing("result" -> "News created")
  }

  def handleUpdate(id: String, data: NewsUpdate) = {
    news.updateNewsItem(id, data.item)
    Redirect(controllers.admin.routes.NewsController.list()).flashing("result" -> "News updated")
  }

  def update(id: String) = RequiredActualUserRoleAction(Sysadmin).async { implicit req =>
      val bound = updateNewsForm.bindFromRequest
      bound.fold(
        errorForm => Future.successful(Ok(views.html.admin.news.updateForm(id, errorForm))),
        data => Future(handleUpdate(id, data))
      )
  }

  def updateForm(id: String) = RequiredActualUserRoleAction(Sysadmin).async {
    val item = news.getNewsItem(id)
    item match {
      case None => Future(NotFound(s"Cannot update news. No news item exists with id '$id'"))
      case Some(news) =>
        for {
          dopts <- departmentOptions
        } yield {
          Ok(views.html.admin.news.updateForm(id, updateNewsForm.fill(NewsUpdate(news.toData))))
        }
    }
  }

  def partitionNews(news: Seq[NewsItemRender]) = news.partition(_.publishDate.isAfterNow)
}
