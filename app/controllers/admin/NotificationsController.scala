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
  activityService: ActivityService
) extends BaseController with I18nSupport with Publishing[NotificationData] {

  import NotificationPublishingService.PROVIDER_ID
  import Roles._
  import securityService._

  val publishNotificationForm = publishForm(mapping(
    "text" -> nonEmptyText,
    "linkHref" -> optional(text).verifying("Invalid URL format", Validation.url)
  )(NotificationData.apply)(NotificationData.unapply))

  def list = RequiredActualUserRoleAction(Sysadmin) {
    val activities = activityService.getActivitiesByProviderId(PROVIDER_ID)

    Ok(views.list(activities))
  }

  def createForm = RequiredActualUserRoleAction(Sysadmin).async {
    departmentOptions.map { dopts =>
      Ok(views.createForm(publishNotificationForm, dopts))
    }
  }

  def create = RequiredActualUserRoleAction(Sysadmin).async { implicit request =>
    departmentOptions.flatMap { dopts =>
      val form = publishNotificationForm.bindFromRequest

      form.fold(
        formWithErrors => Future.successful(Ok(views.createForm(formWithErrors, dopts))),
        publish => {
          audienceBinder.bindAudience(publish).map {
            case Left(errors) =>
              Ok(views.createForm(addFormErrors(form, errors), dopts))
            case Right(Audience.Public) =>
              Ok(views.createForm(form.withGlobalError("Notifications cannot be public"), dopts))
            case Right(audience) =>
              notificationPublishingService.publish(publish.item, audience) match {
                case Success(_) =>
                  Redirect(routes.NotificationsController.list()).flashing("result" -> "Notification created")
                case Failure(e) =>
                  val formWithError = e match {
                    case NoRecipientsException =>
                      form.withGlobalError("The selected audience has no members")
                    case _ =>
                      logger.error("Failure while creating notification", e)
                      form.withGlobalError("An error occurred creating this notification")
                  }

                  Ok(views.createForm(formWithError, dopts))
              }
          }
        }
      )
    }
  }

}
