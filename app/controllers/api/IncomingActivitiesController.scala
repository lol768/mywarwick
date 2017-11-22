package controllers.api

import javax.inject.Singleton

import com.google.inject.Inject
import controllers.MyController
import models.Audience.UsercodesAudience
import models.publishing.Ability.CreateAPINotifications
import models.{Audience, _}
import play.api.i18n.I18nSupport
import play.api.libs.json._
import play.api.mvc.Result
import services.ActivityError.InvalidProviderId
import services._
import warwick.sso.{AuthenticatedRequest, GroupName, User, Usercode}

@Singleton
class IncomingActivitiesController @Inject()(
  securityService: SecurityService,
  activityService: ActivityService,
  publisherService: PublisherService,
  audienceService: AudienceService
) extends MyController with I18nSupport {

  import securityService._

  def postActivity(providerId: String) = APIAction(parse.json) { implicit request =>
    postItem(providerId, shouldNotify = false)
  }

  def postNotification(providerId: String) = APIAction(parse.json) { implicit request =>
    postItem(providerId, shouldNotify = true)
  }

  def postItem(providerId: String, shouldNotify: Boolean)(implicit request: AuthenticatedRequest[JsValue]): Result =
    publisherService.getParentPublisherId(providerId) match {
      case Some(publisherId) =>
        request.context.user.map { user =>
          if (publisherService.getRoleForUser(publisherId, user.usercode).can(CreateAPINotifications)) {
            request.body.validate[IncomingActivityData].map { data =>
              val activity = ActivitySave.fromApi(user.usercode, publisherId, providerId, shouldNotify, data)

              val usercodes = data.recipients.users.getOrElse(Seq.empty).map(Usercode) match {
                case usercodes: Seq[Usercode] if usercodes.nonEmpty => Seq(UsercodesAudience(usercodes.toSet))
                case Nil => Seq.empty[Audience.Component]
              }
              val webgroups = data.recipients.groups.getOrElse(Seq.empty).map(GroupName).map(Audience.WebGroupAudience)

              val audience = Audience(usercodes ++ webgroups)

              val publisher = publisherService.find(publisherId).get
              lazy val recipients = audienceService.resolve(audience).toOption.map(_.size).getOrElse(0)
              publisher.maxRecipients match {
                case Some(max) if shouldNotify && recipients > max =>
                  BadRequest(Json.toJson(API.Failure[JsObject]("bad_request", Seq(API.Error("too-many-recipients", s"You can only send to $max recipients at a time")))))
                case _ =>
                  activityService.save(activity, audience).fold(badRequest, id => {
                    auditLog('CreateActivity, 'id -> id, 'provider -> activity.providerId)
                    created(id)
                  })
              }

            }.recoverTotal(validationError)
          } else {
            forbidden(providerId, user)
          }
        }.get // APIAction calls this only if request.context.user is defined

      case None => badRequest(Seq(InvalidProviderId(providerId)))
    }

  private def badRequest(errors: Seq[ActivityError]): Result =
    BadRequest(Json.toJson(API.Failure[JsObject]("bad_request",
      errors.map(error => API.Error(error.getClass.getSimpleName, error.message))
    )))

  private def created(activityId: String): Result =
    Created(Json.toJson(API.Success("ok", Json.obj(
      "id" -> activityId
    ))))

  private def validationError(error: JsError): Result =
    BadRequest(Json.toJson(API.Failure[JsObject]("bad_request", API.Error.fromJsError(error))))

  private def forbidden(providerId: String, user: User): Result =
    Forbidden(Json.toJson(API.Failure[JsObject]("forbidden",
      Seq(API.Error("no-permission", s"User '${user.usercode.string}' does not have permission to post to the stream for provider '$providerId'"))
    )))

}
