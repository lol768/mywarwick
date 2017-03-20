package controllers.publish

import javax.inject.{Inject, Singleton}

import controllers.BaseController
import models._
import models.news.{Link, NewsItemRender, NewsItemRenderWithAudit, NewsItemSave}
import models.publishing.Ability._
import models.publishing.{Ability, Publisher}
import org.joda.time.LocalDateTime
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{ActionFilter, Result}
import services._
import services.dao.DepartmentInfoDao
import system.{ErrorHandler, RequestContext, TimeZones, Validation}
import uk.ac.warwick.util.web.Uri
import views.html.errors
import warwick.sso.Usercode

import scala.concurrent.Future

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
  val messagesApi: MessagesApi,
  news: NewsService,
  val departmentInfoDao: DepartmentInfoDao,
  val audienceBinder: AudienceBinder,
  val newsCategoryService: NewsCategoryService,
  userNewsCategoryService: UserNewsCategoryService,
  errorHandler: ErrorHandler,
  audienceService: AudienceService,
  userPreferencesService: UserPreferencesService
) extends BaseController with I18nSupport with Publishing {

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

  def newsAudienceForm(implicit request: PublisherRequest[_]) = Form(mapping(
    "item.ignoreCategories" -> boolean,
    "categories" -> categoryMapping,
    "audience" -> audienceMapping
  )(NewsItemAudienceData.apply)(NewsItemAudienceData.unapply))

  def list(publisherId: String) = PublisherAction(publisherId, ViewNews) { implicit request =>
    val theNews = news.getNewsByPublisherWithAudits(publisherId, limit = 100)
    val counts = news.countRecipients(theNews.map(_.id))
    val (newsPending, newsPublished) = partitionNews(theNews)
    Ok(views.html.publish.news.list(request.publisher, newsPending, newsPublished, counts, request.userRole))
  }

  def audienceInfo(publisherId: String) = PublisherAction(publisherId, ViewNews).async { implicit request =>
    audienceForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(Json.toJson(API.Failure[JsObject]("Bad Request", formWithErrors.errors.map(e => API.Error(e.key, e.message)))))),
      audienceData => {
        audienceBinder.bindAudience(audienceData).map {
          case Left(errors) => BadRequest(Json.toJson(API.Failure[JsObject]("Bad Request", errors.map(e => API.Error(e.key, e.message)))))
          case Right(audience) =>
            if (audience.public) {
              Ok(Json.toJson(API.Success[JsObject](data = Json.obj(
                "public" -> true
              ))))
            } else {
              val formData = newsAudienceForm.bindFromRequest.value

              audienceService.resolve(audience)
                .toOption
                .map { usercodesInAudience =>
                  formData.filterNot(_.ignoreCategories)
                    .map { data =>
                      val initialisedUsers = userPreferencesService.countInitialisedUsers(usercodesInAudience)
                      val newsRecipients = userNewsCategoryService.getRecipientsOfNewsInCategories(data.categoryIds).intersect(usercodesInAudience).length

                      Json.obj(
                        "baseAudience" -> usercodesInAudience.length,
                        "categorySubset" -> (usercodesInAudience.length - initialisedUsers + newsRecipients)
                      )
                    }
                    .getOrElse(
                      Json.obj(
                        "baseAudience" -> usercodesInAudience.length
                      )
                    )
                }
                .map(usercodes => Ok(Json.toJson(API.Success[JsObject](data = usercodes))))
                .getOrElse(InternalServerError(Json.toJson(API.Failure[JsObject]("Internal Server Error", Seq(API.Error("resolve-audience", "Failed to resolve audience"))))))
            }
        }
      }
    )
  }

  def createForm(publisherId: String) = PublisherAction(publisherId, CreateNews) { implicit request =>
    Ok(renderCreateForm(request.publisher, publishNewsForm))
  }

  def create(publisherId: String, submitted: Boolean) = PublisherAction(publisherId, CreateNews).async { implicit request =>
    bindFormWithAudience[PublishNewsItemData](publishNewsForm, submitted,
      formWithErrors => Ok(renderCreateForm(request.publisher, formWithErrors)),
      (data, audience) => {
        val newsItem = data.item.toSave(request.context.user.get.usercode, publisherId)
        val newsItemId = news.save(newsItem, audience, data.categoryIds)

        auditLog('CreateNewsItem, 'id -> newsItemId)

        Redirect(controllers.publish.routes.NewsController.list(publisherId)).flashing("result" -> "News created")
      }
    )
  }

  def renderCreateForm(publisher: Publisher, form: Form[PublishNewsItemData])(implicit request: PublisherRequest[_]) = {
    views.html.publish.news.createForm(
      publisher = publisher,
      form = form,
      categories = categoryOptions,
      permissionScope = permissionScope,
      departmentOptions = departmentOptions
    )
  }

  def updateForm(publisherId: String, id: String) = EditAction(id, publisherId, EditNews).async { implicit request =>
    withNewsItem(id, publisherId, item => {
      val categoryIds = newsCategoryService.getNewsCategories(id).map(_.id)
      val audience = news.getAudience(id).map(audienceBinder.unbindAudience).get
      val formWithData = publishNewsForm.fill(PublishNewsItemData(item.toData, categoryIds, audience))

      Future.successful(Ok(renderUpdateForm(publisherId, id, formWithData)))
    })
  }

  def update(publisherId: String, id: String, submitted: Boolean) = EditAction(id, publisherId, EditNews).async { implicit request =>
    withNewsItem(id, publisherId, _ => {
      bindFormWithAudience[PublishNewsItemData](publishNewsForm, submitted,
        formWithErrors => Ok(renderUpdateForm(publisherId, id, formWithErrors)),
        (data, audience) => {
          val newsItem = data.item.toSave(request.context.user.get.usercode, publisherId)

          news.update(id, newsItem, audience, data.categoryIds)
            .onFailure { case e =>
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

  private def NewsBelongsToPublisher(id: String, publisherId: String) = new ActionFilter[PublisherRequest] {
    override protected def filter[A](request: PublisherRequest[A]): Future[Option[Result]] = {
      implicit val r = request

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
  }

  private def withNewsItem(id: String, publisherId: String, block: (NewsItemRender) => Future[Result])(implicit request: RequestContext): Future[Result] = {
    news.getNewsItem(id)
      .filter(_.publisherId == publisherId)
      .map(block)
      .getOrElse(Future.successful(NotFound(views.html.errors.notFound())))
  }

  private def renderUpdateForm(publisherId: String, id: String, form: Form[PublishNewsItemData])(implicit request: PublisherRequest[_]) = {
    views.html.publish.news.updateForm(
      publisher = request.publisher,
      id = id,
      form = form,
      categories = categoryOptions,
      permissionScope = permissionScope,
      departmentOptions = departmentOptions
    )
  }

  private def partitionNews(news: Seq[NewsItemRenderWithAudit]) = news.partition(_.publishDate.isAfterNow)

  private def EditAction(id: String, publisherId: String, ability: Ability) = PublisherAction(publisherId, ability)
    .andThen(NewsBelongsToPublisher(id, publisherId))

}