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

import scala.util.{Failure, Success}

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
          val activity = ActivitySave.fromData(providerId, shouldNotify, data)

          val usercodes = recipientService.getRecipientUsercodes(
            data.recipients.users.getOrElse(Seq.empty).map(Usercode),
            data.recipients.groups.getOrElse(Seq.empty).map(GroupName)
          )

          activityService.save(activity, usercodes) match {
            case Success(activityId) => created(activityId)
            case Failure(NoRecipientsException) => noRecipients
            case Failure(e) =>
              logger.error(s"Error while posting activity providerId=$providerId", e)
              otherError
          }
        }.recoverTotal {
          e => validationError(e)
        }
      } else {
        forbidden(providerId, user)
      }
    }.get // APIAction calls this only if request.context.user is defined

  private def forbidden(providerId: String, user: User): Result =
    Forbidden(Json.toJson(API.Failure[JsObject]("forbidden",
      Seq(API.Error("no-permission", s"User '${user.usercode.string}' does not have permission to post to the stream for provider '$providerId'"))
    )))

  private def created(activityId: String): Result =
    Created(Json.toJson(API.Success("ok", Json.obj(
      "id" -> activityId
    ))))

  private def noRecipients: Result =
    PaymentRequired(Json.toJson(API.Failure[JsObject]("request_failed",
      Seq(API.Error("no-recipients", "No valid recipients for activity"))
    )))

  private def otherError: Result =
    InternalServerError(Json.toJson(API.Failure[JsObject]("internal_server_error",
      Seq(API.Error("internal-error", "An internal error occurred"))
    )))

  private def validationError(error: JsError): Result =
    BadRequest(Json.toJson(API.Failure[JsObject]("bad_request", API.Error.fromJsError(error))))

}
