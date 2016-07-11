package controllers.admin

import javax.inject.{Inject, Singleton}

import controllers.BaseController
import models.PublishingAbility._
import models._
import models.news.{Audience, Link, NewsItemRender, NewsItemSave}
import org.joda.time.LocalDateTime
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{ActionRefiner, Result, WrappedRequest}
import services.dao.DepartmentInfoDao
import services.{NewsCategoryService, NewsService, PublisherService, SecurityService}
import system.{TimeZones, Validation}
import uk.ac.warwick.util.web.Uri
import warwick.sso.{AuthenticatedRequest, Usercode}

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
  def toSave(usercode: Usercode, publisherId: String) = NewsItemSave(
    usercode = usercode,
    publisherId = publisherId,
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
  val securityService: SecurityService,
  val publisherService: PublisherService,
  val messagesApi: MessagesApi,
  news: NewsService,
  val departmentInfoDao: DepartmentInfoDao,
  audienceBinder: AudienceBinder,
  val newsCategoryService: NewsCategoryService
) extends BaseController with I18nSupport with Publishing {

  // Provides implicit conversion from Request to RequestContext
  import securityService._

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

  def list(publisher: String) = PublisherAction(publisher, ViewNews) { implicit request =>
    val theNews = news.getNewsByPublisher(publisher, limit = 100)
    val counts = news.countRecipients(theNews.map(_.id))
    val (newsPending, newsPublished) = partitionNews(theNews)
    Ok(views.html.admin.news.list(publisher, newsPending, newsPublished, counts))
  }

  def createForm(publisher: String) = PublisherAction(publisher, CreateNews) { implicit request =>
    Ok(views.html.admin.news.createForm(publisher, publishNewsForm, departmentOptions, categoryOptions))
  }

  def create(publisher: String) = PublisherAction(publisher, CreateNews).async { implicit request =>
    val bound = publishNewsForm.bindFromRequest
    bound.fold(
      errorForm => Future.successful(Ok(views.html.admin.news.createForm(publisher, errorForm, departmentOptions, categoryOptions))),
      // We only show audience validation errors if there were no other errors, which can look weird.

      data => audienceBinder.bindAudience(data.audience).map {
        case Left(errors) =>
          val errorForm = addFormErrors(bound, errors)
          Ok(views.html.admin.news.createForm(publisher, errorForm, departmentOptions, categoryOptions))
        case Right(audience) =>
          handleForm(request.context.user.get.usercode, publisher, data, audience)
      }
    )
  }

  def handleForm(usercode: Usercode, publisher: String, data: PublishNewsItemData, audience: Audience) = {
    val newsItem = data.item.toSave(usercode, publisher)
    news.save(newsItem, audience, data.categoryIds)
    Redirect(controllers.admin.routes.NewsController.list(publisher)).flashing("result" -> "News created")
  }

  def handleUpdate(usercode: Usercode, publisher: String, id: String, data: UpdateNewsItemData) = {
    val newsItem = data.item.toSave(usercode, publisher)
    news.updateNewsItem(id, newsItem)
    newsCategoryService.updateNewsCategories(id, data.categoryIds)
    Redirect(controllers.admin.routes.NewsController.list(publisher)).flashing("result" -> "News updated")
  }

  def update(publisher: String, id: String) = PublisherAction(publisher, EditNews).async { implicit request =>
    val bound = updateNewsForm.bindFromRequest
    bound.fold(
      errorForm => Future.successful(Ok(views.html.admin.news.updateForm(publisher, id, errorForm, categoryOptions))),
      data => Future(handleUpdate(request.context.user.get.usercode, publisher, id, data))
    )
  }

  def updateForm(publisher: String, id: String) = PublisherAction(publisher, EditNews) { implicit request =>
    news.getNewsItem(id) map { item =>
      val categoryIds = newsCategoryService.getNewsCategories(id).map(_.id)
      Ok(views.html.admin.news.updateForm(publisher, id, updateNewsForm.fill(UpdateNewsItemData(item.toData, categoryIds)), categoryOptions))
    } getOrElse {
      NotFound(s"Cannot update news. No news item exists with id '$id'")
    }
  }

  def partitionNews(news: Seq[NewsItemRender]) = news.partition(_.publishDate.isAfterNow)

}
