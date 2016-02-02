package controllers.api

import com.google.inject.Inject
import controllers.BaseController
import models.{API, LastRead}
import models.API.Error
import org.joda.time.DateTime
import play.api.libs.json._
import services.messaging.APNSOutputService
import services.{ActivityService, SecurityService}

class UserActivitiesController @Inject()(
  activityService: ActivityService,
  securityService: SecurityService,
  apns: APNSOutputService
) extends BaseController {

  import securityService._

  def get = RequiredUserAction { implicit request =>

    val before = request.getQueryString("before").map(date => new DateTime(date.toLong))
    val limit = request.getQueryString("limit").map(_.toInt).getOrElse(20)

    val activities = request.context.user
      .map(user => activityService.getActivitiesForUser(user, limit = limit, before = before))
      .getOrElse(Seq.empty)

    Ok(Json.toJson(API.Success[JsObject](data = Json.obj("activities" -> activities))))

  }

  def getLastRead = RequiredUserAction { implicit request =>
    val response = request.context.user
      .map(u => activityService.getLastReadDate(u).getOrElse(LastRead(u.usercode.string, None, None)))
      .map(lr => API.Success[LastRead](data = lr))
      .getOrElse(
        API.Failure[LastRead]("forbidden", Seq(Error("forbidden", "Cannot fetch last read for anonymous users.")))
      )
    Ok(Json.toJson(response))
  }

  def markAsRead = APIAction(parse.json) { implicit request =>
    request.body.validate[LastRead](LastRead.lastReadFormatter)
      .map( data => {
        request.context.user
          .filter(_.usercode.string == data.usercode)
          .map(u => {
            val success = data.activitiesRead.map(activityService.setActivitiesReadDate(u, _)).getOrElse(true) &&
              data.notificationsRead.map(activityService.setNotificationsReadDate(u, _)).getOrElse(true)
            if (success) {
              apns.clearAppIconBadge(u.usercode)
              Ok(Json.toJson(API.Success[JsObject](data=Json.obj())))
            } else {
              InternalServerError(Json.toJson(
                apiFailure("last-read-noupdate", "The last read date was not updated")
              ))
            }
          })
          .getOrElse(Forbidden(Json.toJson(apiFailure(
            "forbidden",
            s"Cannot mark last read for '${data.usercode}'. You can only mark last read for yourself.")
          )))
      })
      .recoverTotal(e => BadRequest(Json.toJson(API.Failure[JsObject]("error", API.Error.fromJsError(e)))))
  }

  private def apiFailure(id: String, message: String) = API.Failure[JsObject](id, Seq(Error(id, message)))

}
