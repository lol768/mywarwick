package controllers.admin

import javax.inject.Inject

import controllers.BaseController
import models.{Activity, ActivityIcon, ActivityResponse}
import models.news.{Audience, NotificationData}
import models.publishing.Ability.{CreateNotifications, ViewNotifications}
import models.publishing.Publisher
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Result
import services._
import services.dao.DepartmentInfoDao
import system.Validation
import views.html.admin.{notifications => views}

import scala.concurrent.Future
import scala.util.{Failure, Success}

object NotificationsController {
  type PublishNotificationForm = Form[PublishNotificationData]
}

case class PublishNotificationData(item: NotificationData, audience: AudienceData)

class NotificationsController @Inject()(
  val securityService: SecurityService,
  val publisherService: PublisherService,
  val messagesApi: MessagesApi,
  val departmentInfoDao: DepartmentInfoDao,
  audienceBinder: AudienceBinder,
  notificationPublishingService: NotificationPublishingService,
  activityService: ActivityService,
  val newsCategoryService: NewsCategoryService
) extends BaseController with I18nSupport with Publishing {

  import securityService._

  val notificationMapping = mapping(
    "text" -> nonEmptyText,
    "provider" -> nonEmptyText,
    "linkHref" -> optional(text).verifying("Invalid URL format", Validation.url)
  )(NotificationData.apply)(NotificationData.unapply)

  def publishNotificationForm(implicit request: PublisherRequest[_]) = Form(mapping(
    "item" -> notificationMapping,
    "audience" -> audienceMapping
  )(PublishNotificationData.apply)(PublishNotificationData.unapply))

  def list(publisherId: String) = PublisherAction(publisherId, ViewNotifications) { implicit request =>
    val activities = activityService.getActivitiesByPublisherId(publisherId)
      .map(activity => ActivityResponse(
        activity,
        activityService.getActivityIcon(activity.providerId),
        Seq.empty
      ))

    Ok(views.list(request.publisher, activities, request.userRole))
  }

  def createForm(publisherId: String) = PublisherAction(publisherId, CreateNotifications) { implicit request =>
    Ok(renderCreateForm(request.publisher, publishNotificationForm))
  }

  def create(publisherId: String) = PublisherAction(publisherId, CreateNotifications).async { implicit request =>
    val validateOnly = request.body.asFormUrlEncoded.get.contains("validateOnly")
    val form = publishNotificationForm.bindFromRequest

    form.fold(
      formWithErrors => Future.successful(Ok(views.createForm(request.publisher, formWithErrors, departmentOptions, providerOptions, permissionScope))),
      publish => {
        audienceBinder.bindAudience(publish.audience).map {
          case Left(errors) =>
            Ok(views.createForm(request.publisher, addFormErrors(form, errors), departmentOptions, providerOptions, permissionScope))
          case Right(Audience.Public) =>
            Ok(views.createForm(request.publisher, form.withError("audience", "Notifications cannot be public"), departmentOptions, providerOptions, permissionScope))
          case Right(audience) if validateOnly =>
            Ok(renderCreateForm(request.publisher, publishNotificationForm.fill(publish)))
          case Right(audience) =>
            val notification = publish.item.toSave(request.context.user.get.usercode, publisherId)

            val activityId = notificationPublishingService.publish(notification, audience)
            auditLog('CreateNotification, 'id -> activityId)

            Redirect(routes.NotificationsController.list(publisherId)).flashing("result" -> "Notification created")
        }
      }
    )
  }

  def renderCreateForm(publisher: Publisher, form: Form[PublishNotificationData])(implicit request: PublisherRequest[_]) = {
    views.createForm(
      publisher = publisher,
      form = form,
      departmentOptions = departmentOptions,
      providerOptions = providerOptions,
      permissionScope = permissionScope
    )
  }

}
