package controllers.admin

import javax.inject.Inject

import controllers.BaseController
import models.news.NotificationData
import models.publishing.Ability.{CreateNotifications, DeleteNotifications, EditNotifications, ViewNotifications}
import models.{ActivityResponse, Audience, DateFormats}
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{ActionRefiner, Result}
import services._
import services.dao.DepartmentInfoDao
import system.Validation
import views.html.errors
import views.html.admin.{notifications => views}

import scala.concurrent.Future


class NotificationsController @Inject()(
  val securityService: SecurityService,
  val publisherService: PublisherService,
  val messagesApi: MessagesApi,
  val departmentInfoDao: DepartmentInfoDao,
  audienceBinder: AudienceBinder,
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
    "audience" -> audienceMapping
  )(PublishNotificationData.apply)(PublishNotificationData.unapply))

  def list(publisherId: String) = PublisherAction(publisherId, ViewNotifications) { implicit request =>
    val (pastNotifications, futureNotifications) = activityService.getActivitiesByPublisherId(publisherId)
      .map(activity => ActivityResponse(
        activity,
        activityService.getActivityIcon(activity.providerId),
        Seq.empty
      ))
      .partition(_.activity.publishedAt.isBeforeNow)

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

        val activityId = activityService.save(notification, audience)
        auditLog('CreateNotification, 'id -> activityId)

        Redirect(routes.NotificationsController.list(publisherId)).flashing("success" -> "Notification created")
      }
    )
  }

  def updateForm(publisherId: String, id: String) = PublisherAction(publisherId, EditNotifications)
    .andThen(NotificationBelongsToPublisher(id, publisherId))
    .async { implicit request =>
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

  def update(publisherId: String, id: String) = PublisherAction(publisherId, EditNotifications)
    .andThen(NotificationBelongsToPublisher(id, publisherId))
    .async { implicit request =>
      val activity = activityService.getActivityById(id).get

      bindFormWithAudience(
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

  def delete(publisherId: String, id: String) = PublisherAction(publisherId, DeleteNotifications)
    .andThen(NotificationBelongsToPublisher(id, publisherId)) { implicit request =>
      val redirect = Redirect(routes.NotificationsController.list(publisherId))

      activityService.delete(id).fold(
        errors => redirect.flashing("error" -> errors.map(_.message).mkString(", ")),
        _ => {
          auditLog('DeleteNotification, 'id -> id)
          redirect.flashing("success" -> "Notification deleted")
        }
      )
    }

  private def NotificationBelongsToPublisher(id: String, publisherId: String) = new ActionRefiner[PublisherRequest, PublisherRequest] {
    override protected def refine[A](request: PublisherRequest[A]): Future[Either[Result, PublisherRequest[A]]] = {
      implicit val r = request

      val maybeBoolean = for {
        activity <- activityService.getActivityById(id)
        audienceId <- activity.audienceId
      } yield activity.publisherId.contains(publisherId)

      Future.successful {
        if (maybeBoolean.contains(true)) {
          Right(request)
        } else {
          Left(NotFound(errors.notFound()))
        }
      }
    }
  }

  private def bindFormWithAudience(onError: (Form[PublishNotificationData]) => Result, onSuccess: ((PublishNotificationData, Audience) => Result))(implicit request: PublisherRequest[_]): Future[Result] = {
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

}

object NotificationsController {
  type PublishNotificationForm = Form[PublishNotificationData]
}

case class PublishNotificationData(item: NotificationData, audience: AudienceData)
