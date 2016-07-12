package controllers.admin

import javax.inject.Inject

import controllers.BaseController
import models.PublishingAbility
import models.PublishingAbility._
import models.news.{Audience, NotificationData}
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.{I18nSupport, MessagesApi}
import services._
import services.dao.DepartmentInfoDao
import system.{Roles, Validation}
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
    "linkHref" -> optional(text).verifying("Invalid URL format", Validation.url)
  )(NotificationData.apply)(NotificationData.unapply)

  val publishNotificationForm = Form(mapping(
    "item" -> notificationMapping,
    "audience" -> audienceMapping
  )(PublishNotificationData.apply)(PublishNotificationData.unapply))

  def list(publisherId: String) = PublisherAction(publisherId, ViewNotifications) { implicit request =>
    val activities = activityService.getActivitiesByPublisherId(publisherId)

    Ok(views.list(publisherId, activities))
  }

  def createForm(publisherId: String) = PublisherAction(publisherId, CreateNotifications) { implicit request =>
    Ok(views.createForm(publisherId, publishNotificationForm, departmentOptions))
  }

  def create(publisherId: String) = PublisherAction(publisherId, CreateNotifications).async { implicit request =>
    val form = publishNotificationForm.bindFromRequest

    form.fold(
      formWithErrors => Future.successful(Ok(views.createForm(publisherId, formWithErrors, departmentOptions))),
      publish => {
        audienceBinder.bindAudience(publish.audience).map {
          case Left(errors) =>
            Ok(views.createForm(publisherId, addFormErrors(form, errors), departmentOptions))
          case Right(Audience.Public) =>
            Ok(views.createForm(publisherId, form.withError("audience", "Notifications cannot be public"), departmentOptions))
          case Right(audience) =>
            val notification = publish.item.toSave(request.context.user.get.usercode, publisherId)

            notificationPublishingService.publish(notification, audience) match {
              case Success(Right(activityId)) =>
                auditLog('CreateNotification, 'id -> activityId)

                Redirect(routes.NotificationsController.list(publisherId)).flashing("result" -> "Notification created")
              case Success(Left(errors)) =>
                val formWithError = errors.foldLeft(form)((f, error) => f.withGlobalError(error.message))

                Ok(views.createForm(publisherId, formWithError, departmentOptions))
              case Failure(e) =>
                logger.error("Failure while creating notification", e)
                val formWithError = form.withGlobalError("An error occurred creating this notification")

                Ok(views.createForm(publisherId, formWithError, departmentOptions))
            }
        }
      }
    )
  }

}
