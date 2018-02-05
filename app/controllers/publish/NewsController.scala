package controllers.publish

import javax.inject.{Inject, Singleton}

import controllers.MyController
import models._
import models.news.{Link, NewsItemRender, NewsItemRenderWithAuditAndAudience, NewsItemSave}
import models.publishing.Ability._
import models.publishing.{Ability, Publisher}
import org.joda.time.LocalDateTime
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.{Action, ActionFilter, AnyContent, Result}
import services._
import services.dao.DepartmentInfoDao
import system._
import uk.ac.warwick.util.web.Uri
import views.html.errors
import warwick.sso.Usercode

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

case class PublishNewsItemData(item: NewsItemData, categoryIds: Seq[String], audience: AudienceData) extends PublishableWithAudience

case class NewsItemAudienceData(ignoreCategories: Boolean, categoryIds: Seq[String], audience: AudienceData)

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
  news: NewsService,
  val departmentInfoService: DepartmentInfoService,
  val audienceBinder: AudienceBinder,
  val newsCategoryService: NewsCategoryService,
  userNewsCategoryService: UserNewsCategoryService,
  errorHandler: ErrorHandler,
  audienceService: AudienceService,
  userPreferencesService: UserPreferencesService
) extends MyController with I18nSupport with Publishing {

  private val newsItemMapping = mapping(
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

  def newsAudienceForm(implicit request: PublisherRequest[_]) = Form(mapping(
    "item.ignoreCategories" -> boolean,
    "categories" -> categoryMapping,
    "audience" -> audienceMapping
  )(NewsItemAudienceData.apply)(NewsItemAudienceData.unapply))

  def list(publisherId: String) = PublisherAction(publisherId, ViewNews) { implicit request =>
    val theNews = news.getNewsByPublisherWithAuditsAndAudience(publisherId, limit = 100)
    val (newsPending, newsPublished) = partitionNews(theNews)
    Ok(views.html.publish.news.list(request.publisher, newsPending, newsPublished, request.userRole, allDepartments))
  }

  def audienceInfo(publisherId: String): Action[AnyContent] = PublisherAction(publisherId, ViewNews).async {
    implicit request => {
      request.body.asJson
      sharedAudienceInfo(
        SharedAudienceInfoForNews(
          audienceService = audienceService,
          processGroupedUsercodes = PublishingHelper.postProcessGroupedResolvedAudience,
          newsCategories = newsAudienceForm.bindFromRequest.value.flatMap { data =>
            Some(data.categoryIds.map(newsCategoryService.getNewsCategoryForCatId))
          }.getOrElse(Iterable.empty[NewsCategory]).toSet,
          userNewsCategoryService = Some(userNewsCategoryService),
          ignoreNewsCategories = request.body.asFormUrlEncoded.get.get("item.ignoreCategories").map(_.mkString).exists(_.toBoolean)
        )
      )
    }
  }

  def createForm(publisherId: String) = PublisherAction(publisherId, CreateNews) { implicit request =>
    Ok(renderCreateForm(request.publisher, publishNewsForm, Audience()))
  }

  def create(publisherId: String, submitted: Boolean) = PublisherAction(publisherId, CreateNews).async { implicit request =>
    bindFormWithAudience[PublishNewsItemData](publishNewsForm, submitted, restrictedRecipients = false,
      formWithErrors => Ok(renderCreateForm(request.publisher, formWithErrors, Audience())),
      (data, audience) => {
        val newsItem = data.item.toSave(request.context.user.get.usercode, publisherId)
        val newsItemId = news.save(newsItem, audience, data.categoryIds)

        auditLog('CreateNewsItem, 'id -> newsItemId)

        Redirect(controllers.publish.routes.NewsController.list(publisherId)).flashing("result" -> "News created")
      }
    )
  }

  def renderCreateForm(publisher: Publisher, form: Form[PublishNewsItemData], audience: Audience)(implicit request: PublisherRequest[_]) = {
    views.html.publish.news.createForm(
      publisher = publisher,
      form = form,
      categories = categoryOptions,
      permissionScope = permissionScope,
      departmentOptions = departmentOptions,
      audience = audience
    )
  }

  def updateForm(publisherId: String, id: String) = EditAction(id, publisherId, EditNews).async { implicit request =>
    withNewsItem(id, publisherId, item => {
      val categoryIds = newsCategoryService.getNewsCategories(id).map(_.id)
      val audience = news.getAudience(id).get
      val unboundAudience = audienceBinder.unbindAudience(audience)
      val formWithData = publishNewsForm.fill(PublishNewsItemData(item.toData, categoryIds, unboundAudience))

      Future.successful(Ok(renderUpdateForm(publisherId, id, formWithData, audience)))
    })
  }

  def update(publisherId: String, id: String, submitted: Boolean) = EditAction(id, publisherId, EditNews).async { implicit request =>
    withNewsItem(id, publisherId, _ => {
      bindFormWithAudience[PublishNewsItemData](publishNewsForm, submitted, restrictedRecipients = false,
        formWithErrors => Ok(renderUpdateForm(publisherId, id, formWithErrors, Audience())),
        (data, audience) => {
          val newsItem = data.item.toSave(request.context.user.get.usercode, publisherId)

          news.update(id, newsItem, audience, data.categoryIds)
            .failed.foreach { e =>
              logger.error(s"Error updating news item $id", e)
              errorHandler.markInternalServerError()
            }
          auditLog('UpdateNewsItem, 'id -> id)
          Redirect(controllers.publish.routes.NewsController.list(publisherId)).flashing("success" -> "News updated")
        }
      )
    })
  }

  def delete(publisherId: String, id: String) = EditAction(id, publisherId, DeleteNews) { implicit request =>
    news.delete(id)
    auditLog('DeleteNews, 'id -> id)
    Redirect(routes.NewsController.list(publisherId)).flashing("success" -> "News deleted")
  }

  private def NewsBelongsToPublisher(id: String, publisherId: String)(implicit ec: ExecutionContext) =
    new ActionFilter[PublisherRequest] {
      override protected def filter[A](request: PublisherRequest[A]): Future[Option[Result]] = {
        implicit val r: PublisherRequest[A] = request

        val maybeBoolean = for {
          item <- news.getNewsItem(id)
        } yield item.publisherId.contains(publisherId)

        Future.successful {
          if (maybeBoolean.contains(true)) {
            None
          } else {
            Some(NotFound(errors.notFound()))
          }
        }
      }

      override protected def executionContext = ec
    }

  private def withNewsItem(id: String, publisherId: String, block: (NewsItemRender) => Future[Result])(implicit request: RequestContext): Future[Result] = {
    news.getNewsItem(id)
      .filter(_.publisherId == publisherId)
      .map(block)
      .getOrElse(Future.successful(NotFound(views.html.errors.notFound())))
  }

  private def renderUpdateForm(publisherId: String, id: String, form: Form[PublishNewsItemData], audience: Audience)(implicit request: PublisherRequest[_]) = {
    views.html.publish.news.updateForm(
      publisher = request.publisher,
      id = id,
      form = form,
      categories = categoryOptions,
      permissionScope = permissionScope,
      departmentOptions = departmentOptions,
      audience = audience
    )
  }

  private def partitionNews(news: Seq[NewsItemRenderWithAuditAndAudience]) = news.partition(_.publishDate.isAfterNow)

  private def EditAction(id: String, publisherId: String, ability: Ability) = PublisherAction(publisherId, ability)
    .andThen(NewsBelongsToPublisher(id, publisherId))

}
