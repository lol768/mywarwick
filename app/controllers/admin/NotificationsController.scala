package controllers.admin

import javax.inject.Inject

import controllers.BaseController
import models.news.{Audience, NotificationData}
import models.publishing.Ability.{CreateNotifications, DeleteNotifications, EditNotifications, ViewNotifications}
import models.{Activity, ActivityResponse, DateFormats}
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Result
import services._
import services.dao.DepartmentInfoDao
import system.{RequestContext, Validation}
import views.html.errors
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
  val newsCategoryService: NewsCategoryService,
  audienceService: AudienceService,
  publishedNotificationsService: PublishedNotificationsService
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
    bindFormWithAudience(
      formWithErrors =>
        Ok(views.createForm(request.publisher, formWithErrors, departmentOptions, providerOptions, permissionScope)),
      (publish, audience) => {
        val notification = publish.item.toSave(request.context.user.get.usercode, publisherId)

        val activityId = notificationPublishingService.publish(notification, audience)
        auditLog('CreateNotification, 'id -> activityId)

        Redirect(routes.NotificationsController.list(publisherId)).flashing("success" -> "Notification created")
      }
    )
  }

  def updateForm(publisherId: String, id: String) = PublisherAction(publisherId, EditNotifications).async { implicit request =>
    withActivityAndAudience(publisherId, id, (activity, audience) => {
      val notificationData = NotificationData(
        text = activity.title,
        providerId = activity.providerId,
        linkHref = activity.url,
        publishDateSet = true,
        publishDate = activity.generatedAt.toLocalDateTime
      )

      val audienceData = audienceBinder.unbindAudience(audience)

      val form = publishNotificationForm.fill(PublishNotificationData(notificationData, audienceData))

      Future.successful(
        Ok(views.updateForm(request.publisher, activity, form, departmentOptions, providerOptions, permissionScope))
      )
    })
  }

  def update(publisherId: String, id: String) = PublisherAction(publisherId, EditNotifications).async { implicit request =>
    withActivityAndAudience(publisherId, id, (activity, _) => {
      bindFormWithAudience(
        formWithErrors =>
          Ok(views.updateForm(request.publisher, activity, formWithErrors, departmentOptions, providerOptions, permissionScope)),
        (publish, audience) => {
          val redirect = Redirect(routes.NotificationsController.list(publisherId))

          val notification = publish.item.toSave(request.context.user.get.usercode, publisherId)

          notificationPublishingService.update(id, notification, audience).fold(
            errors => redirect.flashing("error" -> errors.map(_.message).mkString(", ")),
            id => {
              auditLog('UpdateNotification, 'id -> id)
              redirect.flashing("success" -> "Notification updated")
            }
          )
        }
      )
    })
  }

  def bindFormWithAudience(onError: (Form[PublishNotificationData]) => Result, onSuccess: ((PublishNotificationData, Audience) => Result))(implicit request: PublisherRequest[_]): Future[Result] = {
    val form = publishNotificationForm.bindFromRequest

    form.fold(
      formWithErrors => Future.successful(onError(formWithErrors)),
      publish => {
        audienceBinder.bindAudience(publish.audience).map {
          case Left(errors) =>
            onError(addFormErrors(form, errors))
          case Right(Audience.Public) =>
            onError(form.withError("audience", "Notifications cannot be public"))
          case Right(audience) =>
            onSuccess(publish, audience)
        }
      }
    )
  }

  def delete(publisherId: String, id: String) = PublisherAction(publisherId, DeleteNotifications).async { implicit request =>
    withActivityAndAudience(publisherId, id, (activity, _) => Future.successful {
      val redirect = Redirect(routes.NotificationsController.list(publisherId))

      notificationPublishingService.delete(id).fold(
        errors => redirect.flashing("error" -> errors.map(_.message).mkString(", ")),
        _ => {
          auditLog('DeleteNotification, 'id -> id)
          redirect.flashing("success" -> "Notification deleted")
        }
      )
    })
  }

  private def withActivityAndAudience(publisherId: String, id: String, block: ((Activity, Audience) => Future[Result]))(implicit request: RequestContext): Future[Result] = {
    (for {
      activity <- activityService.getActivityById(id)
      audienceId <- activity.audienceId
      publishedNotification <- publishedNotificationsService.getByActivityId(id)
      if publishedNotification.publisherId == publisherId
    } yield {
      val audience = audienceService.getAudience(audienceId)

      block(activity, audience)
    }).getOrElse(Future.successful(NotFound(errors.notFound())))
  }

}

object NotificationsController {
  type PublishNotificationForm = Form[PublishNotificationData]
}

case class PublishNotificationData(item: NotificationData, audience: AudienceData)
