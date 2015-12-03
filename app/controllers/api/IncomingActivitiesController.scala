package controllers.api

import com.google.inject.Inject
import models._
import org.joda.time.DateTime
import play.api.i18n.{Messages, MessagesApi, I18nSupport}
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
  providerPermissionService: ProviderPermissionService,
  val messagesApi: MessagesApi
) extends Controller with I18nSupport {

  import securityService._

  def readsPostedActivity(providerId: String, shouldNotify: Boolean): Reads[ActivityPrototype] =
    (Reads.pure(providerId) and
      (__ \ "type").read[String] and
      (__ \ "title").read[String] and
      (__ \ "text").read[String] and
      (__ \ "tags").read[Seq[ActivityTag]].orElse(Reads.pure(Seq.empty)) and
      (__ \ "replace").read[Map[String, String]].orElse(Reads.pure(Map.empty)) and
      (__ \ "generated_at").readNullable[DateTime] and
      Reads.pure(shouldNotify) and
      (__ \ "recipients").read[ActivityRecipients]) (ActivityPrototype.apply _)

  def postActivity(providerId: String) = APIAction(parse.json) { implicit request =>
    postItem(providerId, shouldNotify = false)
  }

  def postNotification(providerId: String) = APIAction(parse.json) { implicit request =>
    postItem(providerId, shouldNotify = true)
  }

  def postItem(providerId: String, shouldNotify: Boolean)(implicit request: AuthenticatedRequest[JsValue]): Result =
    request.context.user.map { user =>
      if (providerPermissionService.canUserPostForProvider(providerId, user)) {
        request.body.validate[ActivityPrototype](readsPostedActivity(providerId, shouldNotify)).map { data =>
          activityService.save(data) match {
            case Success(activityId) => created(activityId)
            case Failure(NoRecipientsException) => noRecipients
            // FIXME we are swallowing almost any exception here
            // differentiate between user error and bug
            // or at least log the exception.
            case Failure(e) => otherError
          }
        }.recoverTotal {
          e => validationError(e)
        }
      } else {
        forbidden(providerId, user)
      }
    }.get // APIAction calls this only if request.context.user is defined

  private def forbidden(providerId: String, user: User): Result =
    Forbidden(API.failure("forbidden",
      "errors" -> Json.arr(
        Json.obj(
          "id" -> "no-permission",
          "message" -> s"User '${user.usercode.string}' does not have permission to post to the stream for provider '$providerId'"
        )
      )
    ))

  private def created(activityId: String): Result =
    Created(API.success(
      "id" -> activityId
    ))

  private def noRecipients: Result =
    PaymentRequired(API.failure("request_failed",
      "errors" -> Json.arr(
        Json.obj(
          "id" -> "no-recipients",
          "message" -> "No valid recipients for activity"
        )
      )
    ))

  private def otherError: Result =
    InternalServerError(API.failure("internal_server_error",
      "errors" -> Json.arr(
        Json.obj(
          "id" -> "internal-error",
          "message" -> "An internal error occurred"
        )
      )
    ))

  private def validationError(error: JsError): Result =
    BadRequest(API.failure("bad_request",
      "errors" -> JsError.toFlatForm(error).map {
        case (field, errors) =>
          val propertyName = field.substring(4) // Remove 'obj.' from start of field name

          Json.obj(
            "id" -> s"invalid-$propertyName",
            "message" -> errors.flatMap(_.messages).map(Messages(_, propertyName)).mkString(", ")
          )
      }
    ))

}
