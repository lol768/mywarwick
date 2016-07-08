package controllers.admin

import javax.inject.{Inject, Singleton}

import controllers.BaseController
import models._
import models.news.{Audience, Link, NewsItemRender, NewsItemSave}
import org.joda.time.LocalDateTime
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.{I18nSupport, MessagesApi}
import services.dao.DepartmentInfoDao
import services.{NewsCategoryService, NewsService, SecurityService}
import system.{Roles, TimeZones, Validation}
import uk.ac.warwick.util.web.Uri

import scala.concurrent.Future

case class PublishNewsItemData(item: NewsItemData, categoryIds: Seq[String], audience: AudienceData)

case class UpdateNewsItemData(item: NewsItemData, categoryIds: Seq[String])

case class NewsItemData(
  title: String,
  text: String,
  linkText: Option[String],
  linkHref: Option[String],
  publishDateSet: Boolean,
  publishDate: LocalDateTime,
  imageId: Option[String],
  ignoreCategories: Boolean = false
) {
  def toSave = NewsItemSave(
    title = title,
    text = text,
    link = for {
      t <- linkText
      h <- linkHref
    } yield Link(t, Uri.parse(h)),
    // TODO test this gives expected results of TZ&DST
    publishDate = (if (publishDateSet) publishDate else LocalDateTime.now).toDateTime(TimeZones.LONDON),
    imageId = imageId,
    ignoreCategories = ignoreCategories
  )
}


@Singleton
class NewsController @Inject()(
  security: SecurityService,
  val messagesApi: MessagesApi,
  news: NewsService,
  val departmentInfoDao: DepartmentInfoDao,
  audienceBinder: AudienceBinder,
  val newsCategoryService: NewsCategoryService
) extends BaseController with I18nSupport with Publishing {

  import Roles._
  import security._

  val newsItemMapping = mapping(
    "title" -> nonEmptyText,
    "text" -> nonEmptyText,
    "linkText" -> optional(text),
    "linkHref" -> optional(text).verifying("Invalid URL format", Validation.url),
    "publishDateSet" -> boolean,
    "publishDate" -> DateFormats.dateTimeLocalMapping,
    "imageId" -> optional(text),
    "ignoreCategories" -> boolean
  )(NewsItemData.apply)(NewsItemData.unapply)

  val publishNewsForm = Form(mapping(
    "item" -> newsItemMapping,
    "categories" -> categoryMapping,
    "audience" -> audienceMapping
  )(PublishNewsItemData.apply)(PublishNewsItemData.unapply))

  val updateNewsForm = Form(mapping(
    "item" -> newsItemMapping,
    "categories" -> categoryMapping
  )(UpdateNewsItemData.apply)(UpdateNewsItemData.unapply))

  def list = RequiredActualUserRoleAction(Sysadmin) { implicit request =>
    val theNews = news.allNews(limit = 100)
    val counts = news.countRecipients(theNews.map(_.id))
    val (newsPending, newsPublished) = partitionNews(theNews)
    Ok(views.html.admin.news.list(newsPending, newsPublished, counts))
  }

  def createForm = RequiredActualUserRoleAction(Sysadmin) { implicit request =>
    Ok(views.html.admin.news.createForm(publishNewsForm, departmentOptions, categoryOptions))
  }

  // TODO drop the sysadmin requirement
  def create = RequiredActualUserRoleAction(Sysadmin).async { implicit request =>
    val bound = publishNewsForm.bindFromRequest
    bound.fold(
      errorForm => Future.successful(Ok(views.html.admin.news.createForm(errorForm, departmentOptions, categoryOptions))),
      // We only show audience validation errors if there were no other errors, which can look weird.

      data => audienceBinder.bindAudience(data.audience).map {
        case Left(errors) =>
          val errorForm = addFormErrors(bound, errors)
          Ok(views.html.admin.news.createForm(errorForm, departmentOptions, categoryOptions))
        case Right(audience) =>
          val newsItem = data.item.toSave
          val newsItemId = news.save(newsItem, audience, data.categoryIds)

          auditLog('CreateNewsItem, 'id -> newsItemId)

          Redirect(controllers.admin.routes.NewsController.list()).flashing("result" -> "News created")
      }
    )
  }

  def update(id: String) = RequiredActualUserRoleAction(Sysadmin) { implicit request =>
    val bound = updateNewsForm.bindFromRequest
    bound.fold(
      errorForm => Ok(views.html.admin.news.updateForm(id, errorForm, categoryOptions)),
      data => {
        news.updateNewsItem(id, data.item)
        newsCategoryService.updateNewsCategories(id, data.categoryIds)

        auditLog('UpdateNewsItem, 'id -> id)

        Redirect(controllers.admin.routes.NewsController.list()).flashing("result" -> "News updated")
      })
  }

  def updateForm(id: String) = RequiredActualUserRoleAction(Sysadmin) { implicit request =>
    news.getNewsItem(id) map { item =>
      val categoryIds = newsCategoryService.getNewsCategories(id).map(_.id)
      Ok(views.html.admin.news.updateForm(id, updateNewsForm.fill(UpdateNewsItemData(item.toData, categoryIds)), categoryOptions))
    } getOrElse {
      NotFound(s"Cannot update news. No news item exists with id '$id'")
    }
  }

  def partitionNews(news: Seq[NewsItemRender]) = news.partition(_.publishDate.isAfterNow)
}
