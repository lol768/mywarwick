package controllers.api

import javax.inject.Singleton

import com.google.inject.Inject
import controllers.BaseController
import models.API.Error
import models._
import org.joda.time.DateTime
import play.api.libs.json._
import services.messaging.MobileOutputService
import services.{ActivityService, SecurityService}
import system.ThreadPools.mobile
import DateFormats.isoDateReads

import scala.concurrent.Future

case class SaveMuteRequest(
  expiresAt: Option[DateTime],
  activityType: Option[String],
  providerId: Option[String],
  tags: Seq[ActivityTag]
)

object SaveMuteRequest {
  implicit val format: OFormat[SaveMuteRequest] = Json.format[SaveMuteRequest]
}

@Singleton
class UserActivitiesController @Inject()(
  activityService: ActivityService,
  securityService: SecurityService,
  mobileOutput: MobileOutputService
) extends BaseController {

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
                .onFailure { case e => logger.warn("clearUnreadCount failure", e) }
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
            activityService.save(ActivityMuteSave.fromRequest(data, user.usercode))
            Ok(Json.toJson(API.Success("ok", "saved")))
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
