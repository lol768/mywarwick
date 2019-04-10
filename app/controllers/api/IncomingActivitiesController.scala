package controllers.api

import java.util.UUID
import java.util.concurrent.TimeUnit

import javax.inject.Singleton
import actors.MessageProcessing.UsersNotFound
import com.google.inject.Inject
import controllers.MyController
import models.Audience.UsercodesAudience
import models.publishing.Ability.CreateAPINotifications
import models.{Audience, _}
import play.api.i18n.I18nSupport
import play.api.libs.json._
import play.api.mvc.{Action, Result}
import services.ActivityError.{InvalidProviderId, InvalidUsercodeAudience}
import services._
import services.messaging._
import warwick.sso.{AuthenticatedRequest, GroupName, User, Usercode}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

@Singleton
class IncomingActivitiesController @Inject()(
  securityService: SecurityService,
  activityService: ActivityService,
  publisherService: PublisherService,
  audienceService: AudienceService,
  messagingService: MessagingService,
  mobileOutputService: MobileOutputService
) extends MyController with I18nSupport {

  import securityService._

  private def canSendTransientPush(publisherId: String, providerId: String, usercode: Usercode): Boolean =
    publisherService.getRoleForUser(publisherId, usercode).can(CreateAPINotifications) &&
      activityService.getProvider(providerId).exists(_.transientPush)

  def transientPushNotification(providerId: String): Action[JsValue] = APIAction(parse.json).async { implicit request =>
    publisherService.getParentPublisherId(providerId) match {
      case Some(publisherId) =>
        val user = request.context.user.get
        if (canSendTransientPush(publisherId, providerId, user.usercode)) {
          request.body.validate[IncomingTransientPushData].map { data =>
            import data._
            val pushNotification: PushNotification =
              PushNotification(
                id =  UUID.randomUUID.toString,
                Payload(title, text, url),
                Some(publisherId),
                providerId,
                `type`,
                ttl = if (ttl.nonEmpty) Some(FiniteDuration(ttl.get.toLong, TimeUnit.SECONDS)) else None,
                channel,
                priority,
                transient = true
              )

            val usercodes: Set[Usercode] = data.recipients.users.getOrElse(Seq.empty).map(Usercode).toSet
            messagingService.processTransientPushNotification(usercodes, pushNotification).map(_.error).map {
              case Some(UsersNotFound(notFound)) => createdWithUsersNotFound(notFound, Seq(InvalidUsercodeAudience(notFound.toSeq)))
              case _ => Created(Json.toJson(API.Success("ok", Json.obj())))
            }
          }.recoverTotal(jsErr => Future.successful(validationError(jsErr)))
        } else
          Future.successful(forbidden(providerId, user))
      case None => Future.successful(badRequest(Seq(InvalidProviderId(providerId))))
    }
  }

  def postActivity(providerId: String): Action[JsValue] = APIAction(parse.json) { implicit request =>
    postItem(providerId, shouldNotify = false)
  }

  def postNotification(providerId: String): Action[JsValue] = APIAction(parse.json) { implicit request =>
    postItem(providerId, shouldNotify = true)
  }

  def postItem(providerId: String, shouldNotify: Boolean)(implicit request: AuthenticatedRequest[JsValue]): Result =
    publisherService.getParentPublisherId(providerId) match {
      case Some(publisherId) =>
        request.context.user.map { user =>
          if (publisherService.getRoleForUser(publisherId, user.usercode).can(CreateAPINotifications)) {
            request.body.validate[IncomingActivityData].map { data =>
              val activity = ActivitySave.fromApi(user.usercode, publisherId, providerId, shouldNotify, data)

              val usercodesAudiences: Seq[UsercodesAudience] = data.recipients.users.getOrElse(Seq.empty).map(Usercode) match {
                case usercodes: Seq[Usercode] if usercodes.nonEmpty => Seq(UsercodesAudience(usercodes.toSet))
                case Nil => Seq.empty[UsercodesAudience]
              }
              if (usercodesAudiences.nonEmpty && usercodesAudiences.forall(_.allUsercodesAreLikelyInvalid)) {
                BadRequest(Json.toJson(API.Failure[JsObject](
                  "bad_request",
                  Seq(API.Error("invalid-usercode", s"All usercodes from this request seem to be invalid")),
                )))
              } else {
                val validUsercodeAudiences = usercodesAudiences.map { usercodesAudience => UsercodesAudience(usercodesAudience.getLikelyValidUsercodes) }

                val warnings: Seq[ActivityError] = if (validUsercodeAudiences.flatten(_.usercodes).size != usercodesAudiences.flatMap(_.usercodes).size) {
                  Seq(InvalidUsercodeAudience(usercodesAudiences.flatMap(_.getLikelyInvalidUsercodes)))
                } else {
                  Seq.empty
                }

                val webGroupAudiences: Seq[Audience.WebGroupAudience] = data.recipients.groups.getOrElse(Seq.empty).map(GroupName).map(Audience.WebGroupAudience)

                val audience: Audience = Audience(validUsercodeAudiences ++ webGroupAudiences)

                val publisher = publisherService.find(publisherId).get
                lazy val recipients = audienceService.resolve(audience).toOption.map(_.size).getOrElse(0)
                publisher.maxRecipients match {
                  case Some(max) if shouldNotify && recipients > max =>
                    BadRequest(Json.toJson(API.Failure[JsObject]("bad_request", Seq(API.Error("too-many-recipients", s"You can only send to $max recipients at a time")))))
                  case _ =>
                    activityService.save(activity, audience).fold(badRequest, id => {
                      auditLog('CreateActivity, 'id -> id, 'provider -> activity.providerId)
                      if (warnings.isEmpty) {
                        created(id)
                      } else {
                        createdWithWarnings(id, warnings)
                      }
                    })
                }
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

  private def created(activityId: String): Result = Created(Json.toJson(API.Success(
    "ok",
    Json.obj("id" -> activityId),
  )))

  private def createdWithWarnings(activityId: String, warnings: Seq[ActivityError]): Result = Created(Json.toJson(API.PartialSuccess(
    "ok",
    Json.obj("id" -> activityId),
    warnings.map(warning => API.Error(warning.getClass.getSimpleName, warning.message)),
  )))

  private def createdWithUsersNotFound(notFoundUsers: Set[Usercode], warnings: Seq[ActivityError]): Result = Created(Json.toJson(API.PartialSuccess(
    "ok",
    Json.obj("notFound" -> notFoundUsers.map(_.string)),
    warnings.map(warning => API.Error(warning.getClass.getSimpleName, warning.message)),
  )))

  private def validationError(error: JsError): Result =
    BadRequest(Json.toJson(API.Failure[JsObject]("bad_request", API.Error.fromJsError(error))))

  private def forbidden(providerId: String, user: User): Result =
    Forbidden(Json.toJson(API.Failure[JsObject]("forbidden",
      Seq(API.Error("no-permission", s"User '${user.usercode.string}' does not have permission to post to the stream for provider '$providerId'"))
    )))
}
