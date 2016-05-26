package controllers.admin

import javax.inject.Inject

import controllers.BaseController
import models.news.{NotificationSave, PublishNotification}
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.{I18nSupport, MessagesApi}
import services.{ActivityService, NoRecipientsException, SecurityService}
import system.{Roles, Validation}

import scala.util.{Failure, Success}

object NotificationsController {
  type PublishNotificationForm = Form[PublishNotification]
}

class NotificationsController @Inject()(
  securityService: SecurityService,
  val messagesApi: MessagesApi,
  activityService: ActivityService
) extends BaseController with I18nSupport {

  import PublishNotification.NEWS_PROVIDER_ID
  import Roles._
  import securityService._

  val publishNotificationForm = Form(
    mapping(
      "item" -> mapping(
        "text" -> nonEmptyText,
        "linkHref" -> optional(text).verifying("Invalid URL format", Validation.url)
      )(NotificationSave.apply)(NotificationSave.unapply),
      "recipients" -> nonEmptyText
    )(PublishNotification.apply)(PublishNotification.unapply)
  )

  def list = RequiredActualUserRoleAction(Sysadmin) {
    val activities = activityService.getActivitiesByProviderId(NEWS_PROVIDER_ID)

    Ok(views.html.admin.notifications.list(activities))
  }

  def createForm = RequiredActualUserRoleAction(Sysadmin) {
    Ok(views.html.admin.notifications.createForm(publishNotificationForm))
  }

  def create = RequiredActualUserRoleAction(Sysadmin) { implicit request =>
    val form = publishNotificationForm.bindFromRequest

    form.fold(
      formWithErrors => Ok(views.html.admin.notifications.createForm(formWithErrors)),
      publishNotification => {
        activityService.save(publishNotification.activityPrototype) match {
          case Success(_) =>
            Redirect(controllers.admin.routes.NotificationsController.list())
          case Failure(e) =>
            val formWithError = e match {
              case NoRecipientsException =>
                form.withError("recipients", "No valid usercodes")
              case _ =>
                logger.error("Failure while creating notification", e)
                form.withGlobalError("An error occurred creating this notification")
            }

            Ok(views.html.admin.notifications.createForm(formWithError))
        }
      }
    )
  }

}
