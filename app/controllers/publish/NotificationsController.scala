package controllers.publish

import javax.inject.Inject

import controllers.BaseController
import models.news.NotificationData
import models.publishing.Ability.{CreateNotifications, DeleteNotifications, EditNotifications, ViewNotifications}
import models.publishing.{Ability, Publisher}
import models.{Audience, DateFormats}
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{ActionFilter, ActionRefiner, Result}
import services._
import services.dao.DepartmentInfoDao
import system.Validation
import views.html.errors
import views.html.publish.{notifications => views}

import scala.concurrent.Future


class NotificationsController @Inject()(
  val securityService: SecurityService,
  val publisherService: PublisherService,
  val messagesApi: MessagesApi,
  val departmentInfoDao: DepartmentInfoDao,
  val audienceBinder: AudienceBinder,
  activityService: ActivityService,
  val newsCategoryService: NewsCategoryService,
  audienceService: AudienceService
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
    "audience" -> audienceMapping.verifying("Notifications cannot be public", !_.audience.contains("Public"))
  )(PublishNotificationData.apply)(PublishNotificationData.unapply))

  def list(publisherId: String) = PublisherAction(publisherId, ViewNotifications) { implicit request =>
    val futureNotifications = activityService.getFutureActivitiesByPublisherId(publisherId)
    val pastNotifications = activityService.getPastActivitiesByPublisherId(publisherId)

    Ok(views.list(request.publisher, futureNotifications, pastNotifications, request.userRole))
  }

  def createForm(publisherId: String) = PublisherAction(publisherId, CreateNotifications) { implicit request =>
    Ok(renderCreateForm(request.publisher, publishNotificationForm))
  }

  def create(publisherId: String, submitted: Boolean) = PublisherAction(publisherId, CreateNotifications).async { implicit request =>
    bindFormWithAudience[PublishNotificationData](publishNotificationForm, submitted,
      formWithErrors =>
        Ok(views.createForm(request.publisher, formWithErrors, departmentOptions, providerOptions, permissionScope)),
      (publish, audience) => {
        val notification = publish.item.toSave(request.context.user.get.usercode, publisherId)
        val redirect = Redirect(routes.NotificationsController.list(publisherId))

        activityService.save(notification, audience).fold(
          errors => redirect.flashing("error" -> errors.map(_.message).mkString(", ")),
          activityId => {
            auditLog('CreateNotification, 'id -> activityId)
            redirect.flashing("success" -> "Notification created")
          }
        )
      }
    )
  }

  def updateForm(publisherId: String, id: String) = EditAction(id, publisherId, EditNotifications).async { implicit request =>
      val activity = activityService.getActivityById(id).get
      val audience = audienceService.getAudience(activity.audienceId.get)

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
        Ok(views.updateForm(request.publisher, activity, form, departmentOptions, providerOptions, permissionScope))
      )
    }

  def update(publisherId: String, id: String, submitted: Boolean) = EditAction(id, publisherId, EditNotifications).async { implicit request =>
      val activity = activityService.getActivityById(id).get

      bindFormWithAudience[PublishNotificationData](publishNotificationForm, submitted,
        formWithErrors =>
          Ok(views.updateForm(request.publisher, activity, formWithErrors, departmentOptions, providerOptions, permissionScope)),
        (publish, audience) => {
          val redirect = Redirect(routes.NotificationsController.list(publisherId))

          val activity = publish.item.toSave(request.context.user.get.usercode, publisherId)

          activityService.update(id, activity, audience).fold(
            errors => redirect.flashing("error" -> errors.map(_.message).mkString(", ")),
            id => {
              auditLog('UpdateNotification, 'id -> id)
              redirect.flashing("success" -> "Notification updated")
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
          redirect.flashing("success" -> "Notification deleted")
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
  }

  def renderCreateForm(publisher: Publisher, form: Form[PublishNotificationData])(implicit request: PublisherRequest[_]) =
    views.createForm(
      publisher = publisher,
      form = form,
      departmentOptions = departmentOptions,
      providerOptions = providerOptions,
      permissionScope = permissionScope
    )

  private def EditAction(id: String, publisherId: String, ability: Ability) = PublisherAction(publisherId, ability)
    .andThen(NotificationBelongsToPublisher(id, publisherId))
}

object NotificationsController {
  type PublishNotificationForm = Form[PublishNotificationData]
}

case class PublishNotificationData(item: NotificationData, audience: AudienceData) extends PublishableWithAudience
