package controllers.api

import com.google.inject.Inject
import controllers.MyController
import javax.inject.{Named, Singleton}
import models.API.Error
import models._
import org.joda.time.DateTime
import play.api.libs.json._
import services.messaging.MobileOutputService
import services.{ActivityService, SecurityService}

import scala.concurrent.{ExecutionContext, Future}

case class SaveMuteRequest(
  expiresAt: Option[DateTime],
  activityType: Option[String],
  providerId: Option[String],
  tags: Seq[ActivityTag]
)

object SaveMuteRequest {
  import DateFormats.isoDateReads
  import DateFormats.isoDateWrites
  implicit val format: OFormat[SaveMuteRequest] = Json.format[SaveMuteRequest]
}

@Singleton
class UserActivitiesController @Inject()(
  activityService: ActivityService,
  securityService: SecurityService,
  mobileOutput: MobileOutputService
)(implicit @Named("web") ec: ExecutionContext) extends MyController {

  import DateFormats.{isoDateReads, isoDateWrites}
  import securityService._

  def notifications(since: Option[String], before: Option[String], limit: Int) = APIAction { implicit request =>
    val notifications = request.context.user
      .map(user => activityService.getNotificationsForUser(user, before, since, limit))
      .getOrElse(Nil)

    Ok(Json.toJson(API.Success(data = Json.obj(
      "notifications" -> notifications,
      "read" -> request.context.user.flatMap(activityService.getLastReadDate)
    ))))
  }

  def activities(since: Option[String], before: Option[String], limit: Int) = APIAction { implicit request =>
    val activities = request.context.user
      .map(user => activityService.getActivitiesForUser(user, before, since, limit))
      .getOrElse(Nil)

    Ok(Json.toJson(API.Success(data = Json.obj(
      "activities" -> activities
    ))))
  }

  def markAsRead = APIAction(parse.json) { implicit request =>
    request.body.validate[Option[DateTime]]((__ \ "lastRead").formatNullable[DateTime])
      .map(data => {
        request.context.user
          .map(u => {
            val success = data.forall(activityService.setLastReadDate(u, _))
            if (success) {
              Future(mobileOutput.clearUnreadCount(u.usercode))
                .failed.foreach { e => logger.warn("clearUnreadCount failure", e) }
              Ok(Json.toJson(API.Success[JsObject](data = Json.obj())))
            }
            else InternalServerError(Json.toJson(
              apiFailure("last-read-noupdate", "The last read date was not updated")
            ))
          })
          .get // APIAction - must be logged in
      })
      .recoverTotal(e => BadRequest(Json.toJson(API.Failure[JsObject]("error", API.Error.fromJsError(e)))))
  }

  def listMutes = RequiredUserAction { request =>
    request.context.user.map { user =>
      Ok(Json.toJson(API.Success[JsObject](data = Json.obj(
        "activityMutes" -> activityService.getActivityMutesForRecipient(user.usercode)
      ))))
    }.get // RequiredUserAction
  }

  def saveMute = RequiredUserAction { request =>
    request.context.user.map { user =>
      request.body.asJson.map { body =>
        body.validate[SaveMuteRequest] match {
          case JsSuccess(data, _) =>
            val newMute = ActivityMuteSave.fromRequest(data, user.usercode)
            val existingMute = activityService.getActivityMutesForRecipient(user.usercode).find(mute =>
              mute.provider.map(_.id) == newMute.providerId &&
              mute.activityType.map(_.name) == newMute.activityType &&
              mute.tags.lengthCompare(newMute.tags.length) == 0 &&
              mute.tags.forall(newMute.tags.contains)
            )
            existingMute match {
              case Some(mute) =>
                activityService.update(mute.id, newMute).fold(
                  errors => BadRequest(Json.toJson(
                    API.Failure[JsObject]("error", errors.map(error => API.Error(error.getClass.getSimpleName, error.message)))
                  )),
                  _ => Ok(Json.toJson(API.Success("ok", "saved")))
                )
              case _ =>
                activityService.save(newMute).fold(
                  errors => BadRequest(Json.toJson(
                    API.Failure[JsObject]("error", errors.map(error => API.Error(error.getClass.getSimpleName, error.message)))
                  )),
                  _ => Ok(Json.toJson(API.Success("ok", "saved")))
                )
            }
          case error: JsError =>
            BadRequest(Json.toJson(API.Failure[JsObject]("error", API.Error.fromJsError(error))))
        }
      }.getOrElse(BadRequest(Json.toJson(API.Failure[JsObject]("bad request", Seq(API.Error("invalid-body", "Body must be JSON-formatted SaveMuteRequest"))))))
    }.get // RequiredUserAction
  }

  def removeMute(id: String) = RequiredUserAction { implicit request =>
    request.context.user.map { user =>
      activityService.expireActivityMute(user.usercode, id).fold(
        errors => BadRequest(Json.toJson(
          API.Failure[JsObject]("error", errors.map(error => API.Error(error.getClass.getSimpleName, error.message)))
        )),
        id => {
          auditLog('RemoveMute, 'id -> id)
          Ok(Json.toJson(API.Success("ok", "removed")))
        }
      )
    }.get // RequiredUserAction
  }

  private def apiFailure(id: String, message: String) = API.Failure[JsObject](id, Seq(Error(id, message)))

}
