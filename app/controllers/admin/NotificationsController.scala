package controllers.admin

import javax.inject.Inject

import controllers.BaseController
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
  type PublishNotificationForm = Form[Publish[NotificationData]]
}

class NotificationsController @Inject()(
  securityService: SecurityService,
  val messagesApi: MessagesApi,
  val departmentInfoDao: DepartmentInfoDao,
  audienceBinder: AudienceBinder,
  notificationPublishingService: NotificationPublishingService,
  activityService: ActivityService,
  val newsCategoryService: NewsCategoryService
) extends BaseController with I18nSupport with Publishing[NotificationData] {

  import NotificationPublishingService.PROVIDER_ID
  import Roles._
  import securityService._

  val publishNotificationForm = publishForm(categoriesRequired = false, mapping(
    "text" -> nonEmptyText,
    "linkHref" -> optional(text).verifying("Invalid URL format", Validation.url)
  )(NotificationData.apply)(NotificationData.unapply))

  def list = RequiredActualUserRoleAction(Sysadmin) {
    val activities = activityService.getActivitiesByProviderId(PROVIDER_ID)

    Ok(views.list(activities))
  }

  def createForm = RequiredActualUserRoleAction(Sysadmin) {
    Ok(views.createForm(publishNotificationForm, departmentOptions))
  }

  def create = RequiredActualUserRoleAction(Sysadmin).async { implicit request =>

    val form = publishNotificationForm.bindFromRequest

    form.fold(
      formWithErrors => Future.successful(Ok(views.createForm(formWithErrors, departmentOptions))),
      publish => {
        audienceBinder.bindAudience(publish).map {
          case Left(errors) =>
            Ok(views.createForm(addFormErrors(form, errors), departmentOptions))
          case Right(Audience.Public) =>
            Ok(views.createForm(form.withError("audience", "Notifications cannot be public"), departmentOptions))
          case Right(audience) =>
            notificationPublishingService.publish(publish.item, audience) match {
              case Success(Right(_)) =>
                Redirect(routes.NotificationsController.list()).flashing("result" -> "Notification created")
              case Success(Left(errors)) =>
                val formWithError = errors.foldLeft(form)((f, error) => f.withGlobalError(error.message))

                Ok(views.createForm(formWithError, departmentOptions))
              case Failure(e) =>
                logger.error("Failure while creating notification", e)
                val formWithError = form.withGlobalError("An error occurred creating this notification")

                Ok(views.createForm(formWithError, departmentOptions))
            }
        }
      }
    )

  }

}
