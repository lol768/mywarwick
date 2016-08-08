package controllers.admin

import javax.inject.Inject

import controllers.BaseController
import models.news.{Audience, NotificationData}
import models.publishing.Ability.{CreateNotifications, ViewNotifications}
import models.{ActivityResponse, DateFormats}
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.{I18nSupport, MessagesApi}
import services._
import services.dao.DepartmentInfoDao
import system.Validation
import views.html.admin.{notifications => views}

import scala.concurrent.Future


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

  val notificationMapping = mapping(
    "text" -> nonEmptyText,
    "provider" -> nonEmptyText,
    "linkHref" -> optional(text).verifying("Invalid URL format", Validation.url),
    "publishDateSet" -> boolean,
    "publishDate" -> DateFormats.dateTimeLocalMapping
  )(NotificationData.apply)(NotificationData.unapply)

  def publishNotificationForm(implicit request: PublisherRequest[_]) = Form(mapping(
    "item" -> notificationMapping,
    "audience" -> audienceMapping
  )(PublishNotificationData.apply)(PublishNotificationData.unapply))

  def list(publisherId: String) = PublisherAction(publisherId, ViewNotifications) { implicit request =>
    val (pastNotifications, futureNotifications) = activityService.getActivitiesByPublisherId(publisherId)
      .map(activity => ActivityResponse(
        activity,
        activityService.getActivityIcon(activity.providerId),
        Seq.empty
      ))
      .partition(_.activity.generatedAt.isBeforeNow)

    Ok(views.list(request.publisher, futureNotifications, pastNotifications, request.userRole))
  }

  def createForm(publisherId: String) = PublisherAction(publisherId, CreateNotifications) { implicit request =>
    Ok(views.createForm(request.publisher, publishNotificationForm, departmentOptions, providerOptions, permissionScope))
  }

  def create(publisherId: String) = PublisherAction(publisherId, CreateNotifications).async { implicit request =>
    val form = publishNotificationForm.bindFromRequest

    form.fold(
      formWithErrors => Future.successful(Ok(views.createForm(request.publisher, formWithErrors, departmentOptions, providerOptions, permissionScope))),
      publish => {
        audienceBinder.bindAudience(publish.audience).map {
          case Left(errors) =>
            Ok(views.createForm(request.publisher, addFormErrors(form, errors), departmentOptions, providerOptions, permissionScope))
          case Right(Audience.Public) =>
            Ok(views.createForm(request.publisher, form.withError("audience", "Notifications cannot be public"), departmentOptions, providerOptions, permissionScope))
          case Right(audience) =>
            val notification = publish.item.toSave(request.context.user.get.usercode, publisherId)

            val activityId = notificationPublishingService.publish(notification, audience)
            auditLog('CreateNotification, 'id -> activityId)

            Redirect(routes.NotificationsController.list(publisherId)).flashing("result" -> "Notification created")
        }
      }
    )
  }

}

object NotificationsController {
  type PublishNotificationForm = Form[PublishNotificationData]
}

case class PublishNotificationData(item: NotificationData, audience: AudienceData)
