package controllers.admin

import javax.inject.{Inject, Singleton}

import controllers.BaseController
import models.PublishingAbility._
import models._
import models.news.{Link, NewsItemRender, NewsItemSave}
import org.joda.time.LocalDateTime
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.{I18nSupport, MessagesApi}
import services.dao.DepartmentInfoDao
import services.{NewsCategoryService, NewsService, PublisherService, SecurityService}
import system.{TimeZones, Validation}
import uk.ac.warwick.util.web.Uri
import warwick.sso.Usercode

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

  def publishNewsForm(implicit request: PublisherRequest[_]) = Form(mapping(
    "item" -> newsItemMapping,
    "categories" -> categoryMapping,
    "audience" -> audienceMapping
  )(PublishNewsItemData.apply)(PublishNewsItemData.unapply))

  val updateNewsForm = Form(mapping(
    "item" -> newsItemMapping,
    "categories" -> categoryMapping
  )(UpdateNewsItemData.apply)(UpdateNewsItemData.unapply))

  def list(publisherId: String) = PublisherAction(publisherId, ViewNews) { implicit request =>
    val theNews = news.getNewsByPublisher(publisherId, limit = 100)
    val counts = news.countRecipients(theNews.map(_.id))
    val (newsPending, newsPublished) = partitionNews(theNews)
    Ok(views.html.admin.news.list(publisherId, newsPending, newsPublished, counts))
  }

  def createForm(publisherId: String) = PublisherAction(publisherId, CreateNews) { implicit request =>
    Ok(renderCreateForm(publisherId, publishNewsForm))
  }

  def create(publisherId: String) = PublisherAction(publisherId, CreateNews).async { implicit request =>
    val bound = publishNewsForm.bindFromRequest
    bound.fold(
      errorForm => Future.successful(Ok(renderCreateForm(publisherId, errorForm))),
      // We only show audience validation errors if there were no other errors, which can look weird.

      data => audienceBinder.bindAudience(data.audience).map {
        case Left(errors) =>
          val errorForm = addFormErrors(bound, errors)
          Ok(renderCreateForm(publisherId, errorForm))
        case Right(audience) =>
          val newsItem = data.item.toSave(request.context.user.get.usercode, publisherId)
          val newsItemId = news.save(newsItem, audience, data.categoryIds)

          auditLog('CreateNewsItem, 'id -> newsItemId)

          Redirect(controllers.admin.routes.NewsController.list(publisherId)).flashing("result" -> "News created")
      }
    )
  }

  def renderCreateForm(publisherId: String, form: Form[PublishNewsItemData])(implicit request: PublisherRequest[_]) = {
    views.html.admin.news.createForm(
      publisher = publisherId,
      form = form,
      categories = categoryOptions,
      permissionScope = permissionScope,
      departmentOptions = departmentOptions
    )
  }

  def updateForm(publisher: String, id: String) = PublisherAction(publisher, EditNews) { implicit request =>
    news.getNewsItem(id).map { item =>
      val categoryIds = newsCategoryService.getNewsCategories(id).map(_.id)
      val formWithData = updateNewsForm.fill(UpdateNewsItemData(item.toData, categoryIds))

      Ok(renderUpdateForm(publisher, id, formWithData))
    }.getOrElse {
      NotFound(views.html.errors.notFound())
    }
  }

  def update(publisherId: String, id: String) = PublisherAction(publisherId, EditNews) { implicit request =>
    val bound = updateNewsForm.bindFromRequest
    bound.fold(
      errorForm => Ok(renderUpdateForm(publisherId, id, errorForm)),
      data => {
        val newsItem = data.item.toSave(request.context.user.get.usercode, publisherId)

        news.updateNewsItem(id, newsItem)
        newsCategoryService.updateNewsCategories(id, data.categoryIds)

        auditLog('UpdateNewsItem, 'id -> id)

        Redirect(controllers.admin.routes.NewsController.list(publisherId)).flashing("result" -> "News updated")
      })
  }

  def renderUpdateForm(publisherId: String, id: String, form: Form[UpdateNewsItemData])(implicit request: PublisherRequest[_]) = {
    views.html.admin.news.updateForm(
      publisher = publisherId,
      id = id,
      form = form,
      categories = categoryOptions
    )
  }

  def partitionNews(news: Seq[NewsItemRender]) = news.partition(_.publishDate.isAfterNow)

}
