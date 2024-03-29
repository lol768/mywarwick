package controllers.publish

import com.google.inject.name.Named
import controllers.MyController
import javax.inject.Inject
import models.news.NotificationData
import models.publishing.Ability._
import models.publishing.{Ability, Publisher}
import models.{Audience, DateFormats}
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.I18nSupport
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, ActionFilter, AnyContent, Result}
import services._
import services.elasticsearch.ActivityESService
import system.Validation
import views.html.errors
import views.html.publish.{notifications => views}

import scala.concurrent.{ExecutionContext, Future}

class NotificationsController @Inject()(
  val securityService: SecurityService,
  val publisherService: PublisherService,
  val departmentInfoService: DepartmentInfoService,
  val audienceBinder: AudienceBinder,
  activityService: ActivityService,
  val newsCategoryService: NewsCategoryService,
  audienceService: AudienceService,
  activityESService: ActivityESService
)(implicit @Named("web") webEC: ExecutionContext) extends MyController with I18nSupport with Publishing {

  override protected val ec: ExecutionContext = webEC

  private val urlPattern = """.*https{0,1}://[^\s]+.*""".r

  val notificationMapping: Mapping[NotificationData] = mapping(
    "text" -> nonEmptyText.verifying("The URL should be put in the Link URL field so that it is clickable", t => urlPattern.findFirstIn(t).isEmpty),
    "provider" -> nonEmptyText,
    "linkHref" -> optional(text).verifying("Invalid URL format", Validation.url),
    "publishDateSet" -> boolean,
    "publishDate" -> DateFormats.dateTimeLocalMapping
  )(NotificationData.apply)(NotificationData.unapply)

  def publishNotificationForm(implicit request: PublisherRequest[_]) = Form(mapping(
    "item" -> notificationMapping,
    "audience" -> audienceMapping.verifying("Alerts cannot be public", !_.audience.contains("Public"))
  )(PublishNotificationData.apply)(PublishNotificationData.unapply))

  def list(publisherId: String): Action[AnyContent] = PublisherAction(publisherId, ViewNotifications) { implicit request => {
    val futureNotifications = activityService.getFutureActivitiesWithAudienceByPublisherId(publisherId, includeApiUser = false)
    val sendingNotifications = activityService.getSendingActivitiesWithAudienceByPublisherId(publisherId, includeApiUser = false)
    val pastNotifications = activityService.getPastActivitiesWithAudienceByPublisherId(publisherId, includeApiUser = false)
    Ok(views.list(request.publisher, futureNotifications, sendingNotifications, pastNotifications, request.userRole, allDepartments))
  }}

  def audienceInfo(publisherId: String): Action[AnyContent] = PublisherAction(publisherId, ViewNotifications).async { implicit request =>
    sharedAudienceInfo(SharedAudienceInfoForNotifications(audienceService, AudienceInfoHelper.postProcessGroupedResolvedAudience))
  }

  def status(publisherId: String, activityId: String) = PublisherAction(publisherId, ViewNotifications).async { implicit request => {
    val activityWithAudience = activityService.getActivityWithAudience(activityId).filter(_.activity.publisherId.contains(publisherId))

    activityESService.deliveryReportForActivity(activityId, activityWithAudience.map(_.activity.publishedAt)).map { deliveryReport =>
      activityWithAudience.map { activity =>
        Ok(Json.obj(
          "audienceSize" -> activity.audienceSize.toOption,
          "sent" -> (
            Json.obj("total" -> activity.sentCount)
              ++ Json.obj("readCount" -> activityService.getActivityReadCountSincePublishedDate(activityId))
              ++ deliveryReport.successful.map(count => Json.obj("delivered" -> count)).getOrElse(JsObject(Nil))
            ),
          "sendingNow" -> activity.isSendingNow
        ))
      }.getOrElse(NotFound(Json.obj("error" -> "not_found")))
    }
  }}

  def createForm(publisherId: String) = PublisherAction(publisherId, CreateNotifications) { implicit request =>
    Ok(renderCreateForm(request.publisher, publishNotificationForm, Audience()))
  }

  def create(publisherId: String, submitted: Boolean) = PublisherAction(publisherId, CreateNotifications).async { implicit request =>
    bindFormWithAudience[PublishNotificationData](publishNotificationForm, submitted, restrictedRecipients = true,
      formWithErrors =>
        Ok(views.createForm(request.publisher, formWithErrors, departmentOptions, publisherProviders, permissionScope, Audience())),
      (publish, audience) => {
        val notification = publish.item.toSave(request.context.user.get.usercode, publisherId)
        val redirect = Redirect(routes.NotificationsController.list(publisherId))

        activityService.save(notification, audience).fold(
          errors => redirect.flashing("error" -> errors.map(_.message).mkString(", ")),
          activityId => {
            auditLog('CreateNotification, 'id -> activityId)
            redirect.flashing("success" -> "Alert created")
          }
        )
      }
    )
  }

  def updateForm(publisherId: String, id: String) = EditAction(id, publisherId, EditNotifications).async { implicit request =>
    val activity = activityService.getActivityById(id).get
    val audience = audienceService.getAudience(activity.audienceId.get)
    val audienceJson = audienceService.audienceToJson(audience)

    val notificationData = NotificationData(
      text = activity.title,
      providerId = activity.providerId,
      linkHref = activity.url,
      publishDateSet = true,
      publishDate = activity.publishedAt.toLocalDateTime
    )

    val audienceData = audienceBinder.unbindAudience(audience)

    val form = publishNotificationForm.fill(PublishNotificationData(notificationData, audienceData))

    Future.successful(
      Ok(views.updateForm(request.publisher, activity, form, departmentOptions, publisherProviders, permissionScope, audience, audienceJson))
    )
  }

  def update(publisherId: String, id: String, submitted: Boolean) = EditAction(id, publisherId, EditNotifications).async { implicit request =>
    val activity = activityService.getActivityById(id).get
    val audience = audienceService.getAudience(activity.id)
    val audienceJson = audienceService.audienceToJson(audience)

    bindFormWithAudience[PublishNotificationData](publishNotificationForm, submitted, restrictedRecipients = true,
      formWithErrors =>
        Ok(views.updateForm(request.publisher, activity, formWithErrors, departmentOptions, publisherProviders, permissionScope, audience, audienceJson)),
      (publish, audience) => {
        val redirect = Redirect(routes.NotificationsController.list(publisherId))

        val activity = publish.item.toSave(request.context.user.get.usercode, publisherId)

        activityService.update(id, activity, audience).fold(
          errors => redirect.flashing("error" -> errors.map(_.message).mkString(", ")),
          id => {
            auditLog('UpdateNotification, 'id -> id)
            redirect.flashing("success" -> "Alert updated")
          }
        )
      }
    )
  }

  def delete(publisherId: String, id: String) = EditAction(id, publisherId, DeleteNotifications) { implicit request =>
    val redirect = Redirect(routes.NotificationsController.list(publisherId))

    activityService.delete(id).fold(
      errors => redirect.flashing("error" -> errors.map(_.message).mkString(", ")),
      _ => {
        auditLog('DeleteNotification, 'id -> id)
        redirect.flashing("success" -> "Alert deleted")
      }
    )
  }

  private def NotificationBelongsToPublisher(id: String, publisherId: String) = new ActionFilter[PublisherRequest] {

    override protected def filter[A](request: PublisherRequest[A]): Future[Option[Result]] = {
      implicit val r = request
      val maybeBoolean = for {
        activity <- activityService.getActivityById(id)
        audienceId <- activity.audienceId
      } yield activity.publisherId.contains(publisherId)

      Future.successful {
        if (maybeBoolean.contains(true)) {
          None
        } else {
          Some(NotFound(errors.notFound()))
        }
      }
    }

    override protected def executionContext: ExecutionContext = ec
  }

  def renderCreateForm(publisher: Publisher, form: Form[PublishNotificationData], audience: Audience)(implicit request: PublisherRequest[_]) =
    views.createForm(
      publisher = publisher,
      form = form,
      departmentOptions = departmentOptions,
      providers = publisherProviders,
      permissionScope = permissionScope,
      audience = audience
    )

  private def EditAction(id: String, publisherId: String, ability: Ability) = PublisherAction(publisherId, ability)
    .andThen(NotificationBelongsToPublisher(id, publisherId))
}

object NotificationsController {
  type PublishNotificationForm = Form[PublishNotificationData]
}

case class PublishNotificationData(item: NotificationData, audience: AudienceData) extends PublishableWithAudience
