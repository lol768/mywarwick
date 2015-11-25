package controllers.api

import com.google.inject.Inject
import models.{ActivityRecipients, ActivityTag, PostedActivity, TagValue}
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.mvc.{Controller, Result}
import services.{ActivityService, ProviderPermissionService, NoRecipientsException, SecurityService}
import warwick.sso.{AuthenticatedRequest, User}

import scala.util.{Failure, Success}

class IncomingActivitiesController @Inject()(
  securityService: SecurityService,
  activityService: ActivityService,
  providerPermissionService: ProviderPermissionService
) extends Controller {

  import securityService._

  implicit val readsActivityRecipients = Json.reads[ActivityRecipients]

  implicit val readsActivityTag: Reads[ActivityTag] = (
    (__ \ "name").read[String] and
      __.read[TagValue]((
        (__ \ "value").read[String] and
          (__ \ "display_value").readNullable[String]
        ) (TagValue))
    ) (ActivityTag)

  implicit val readsPostedActivity = Json.reads[PostedActivity]

  def postActivity(providerId: String) = APIAction(parse.json) { implicit request =>
    postItem(providerId, shouldNotify = false)
  }

  def postNotification(providerId: String) = APIAction(parse.json) { implicit request =>
    postItem(providerId, shouldNotify = true)
  }

  def postItem(providerId: String, shouldNotify: Boolean)(implicit request: AuthenticatedRequest[JsValue]): Result =
    request.context.user.map { user =>
      if (providerPermissionService.canUserPostForProvider(providerId, user)) {
        request.body.validate[PostedActivity].map { data =>
          activityService.save(data.toActivityPrototype(providerId, shouldNotify)) match {
            case Success(activityId) => created(activityId)
            case Failure(_: NoRecipientsException) => noRecipients
            case Failure(_) => otherError
          }
        }.recoverTotal {
          e => validationError(e)
        }
      } else {
        forbidden(providerId, user)
      }
    }.get // APIAction calls this only if request.context.user is defined

  private def forbidden(providerId: String, user: User): Result =
    Forbidden(Json.obj(
      "success" -> false,
      "status" -> "forbidden",
      "errors" -> Json.arr(
        Json.obj(
          "message" -> s"User '${user.usercode.string}' does not have permission to post to the stream for provider '$providerId'"
        )
      )
    ))

  private def created(activityId: String): Result =
    Created(Json.obj(
      "success" -> true,
      "status" -> "ok",
      "id" -> activityId
    ))

  private def noRecipients: Result =
    PaymentRequired(Json.obj(
      "success" -> false,
      "status" -> "request_failed",
      "errors" -> Json.arr(
        Json.obj(
          "message" -> "No valid recipients for activity"
        )
      )
    ))

  private def otherError: Result =
    InternalServerError(Json.obj(
      "success" -> false,
      "status" -> "internal_server_error"
    ))

  private def validationError(error: JsError): Result =
    BadRequest(Json.obj(
      "success" -> false,
      "status" -> "bad_request",
      "errors" -> JsError.toJson(error)
    ))

}
