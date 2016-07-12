package controllers.api

import javax.inject.Singleton

import com.google.inject.Inject
import controllers.BaseController
import models._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json._
import play.api.mvc.Result
import services._
import warwick.sso.{AuthenticatedRequest, GroupName, User, Usercode}

@Singleton
class IncomingActivitiesController @Inject()(
  securityService: SecurityService,
  activityService: ActivityService,
  recipientService: ActivityRecipientService,
  providerPermissionService: ProviderPermissionService,
  val messagesApi: MessagesApi
) extends BaseController with I18nSupport {

  import securityService._

  def postActivity(providerId: String) = APIAction(parse.json) { implicit request =>
    postItem(providerId, shouldNotify = false)
  }

  def postNotification(providerId: String) = APIAction(parse.json) { implicit request =>
    postItem(providerId, shouldNotify = true)
  }

  def postItem(providerId: String, shouldNotify: Boolean)(implicit request: AuthenticatedRequest[JsValue]): Result =
    request.context.user.map { user =>
      if (providerPermissionService.canUserPostForProvider(providerId, user)) {
        request.body.validate[IncomingActivityData].map { data =>
          val activity = ActivitySave.fromApi(providerId, shouldNotify, data)

          val usercodes = recipientService.getRecipientUsercodes(
            data.recipients.users.getOrElse(Seq.empty).map(Usercode),
            data.recipients.groups.getOrElse(Seq.empty).map(GroupName)
          )

          activityService.save(activity, usercodes).fold(badRequest, id => {
            auditLog('CreateActivity, 'id -> id, 'provider -> activity.providerId)
            created(id)
          })
        }.recoverTotal {
          e => validationError(e)
        }
      } else {
        forbidden(providerId, user)
      }
    }.get // APIAction calls this only if request.context.user is defined

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
