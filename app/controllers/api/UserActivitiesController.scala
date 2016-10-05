package controllers.api

import javax.inject.{Named, Singleton}

import com.google.inject.Inject
import controllers.BaseController
import models.API.Error
import models.{API, DateFormats}
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import play.api.libs.json._
import services.messaging.{APNSOutputService, MobileOutputService}
import services.{ActivityService, SecurityService}
import system.ThreadPools.mobile

import scala.concurrent.Future

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
      .map(user => activityService.getActivitiesForUser(user, before, since))
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

  private def apiFailure(id: String, message: String) = API.Failure[JsObject](id, Seq(Error(id, message)))

}
