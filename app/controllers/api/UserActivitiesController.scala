package controllers.api

import javax.inject.Singleton

import com.google.inject.Inject
import controllers.BaseController
import models.API.Error
import models.{API, DateFormats}
import org.joda.time.DateTime
import play.api.libs.json._
import services.messaging.APNSOutputService
import services.{ActivityService, SecurityService}
import system.ThreadPools.mobile

import scala.concurrent.Future

@Singleton
class UserActivitiesController @Inject()(
  activityService: ActivityService,
  securityService: SecurityService,
  apns: APNSOutputService
) extends BaseController {

  import DateFormats.{isoDateReads, isoDateWrites}
  import securityService._

  def get = RequiredUserAction { implicit request =>
    val before = request.getQueryString("before").map(date => new DateTime(date.toLong))
    val limit = request.getQueryString("limit").map(_.toInt).getOrElse(20)

    val activities = request.context.user
      .map(user => activityService.getActivitiesForUser(user, limit, before))
      .getOrElse(Seq.empty)

    val data = Json.obj(
      "activities" -> activities,
      "notificationsRead" -> request.context.user.flatMap(activityService.getLastReadDate)
    )

    Ok(Json.toJson(API.Success[JsObject](data = data)))
  }

  def markAsRead = APIAction(parse.json) { implicit request =>
    request.body.validate[Option[DateTime]]((__ \ "lastRead").formatNullable[DateTime])
      .map(data => {
        request.context.user
          .map(u => {
            val success = data.forall(activityService.setLastReadDate(u, _))
            if (success) {
              Future(apns.clearAppIconBadge(u.usercode))
                .onFailure { case e => logger.warn("apns.clearAppIconBadge failure", e) }
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
