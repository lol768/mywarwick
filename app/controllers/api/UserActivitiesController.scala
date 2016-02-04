package controllers.api

import com.google.inject.Inject
import controllers.BaseController
import models.{API, LastRead}
import models.API.Error
import org.joda.time.DateTime
import play.api.libs.json._
import services.{ActivityService, SecurityService}

class UserActivitiesController @Inject()(
  activityService: ActivityService,
  securityService: SecurityService
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
      .map(u => LastRead(u.usercode.string, activityService.getLastReadDate(u)))
      .map(lr => API.Success[LastRead](data = lr))
      .getOrElse(
        API.Failure[LastRead]("forbidden", Seq(Error("forbidden", "Cannot fetch last read for anonymous users.")))
      )
    Ok(Json.toJson(response))
  }

  import models.DateFormats.isoDateReads

  def markAsRead = APIAction(parse.json) { implicit request =>
    request.body.validate[Option[DateTime]]((__ \ "lastRead").formatNullable[DateTime])
      .map( data => {
        request.context.user
          .map(u => {
            val success = data.map(activityService.setLastReadDate(u, _)).getOrElse(true)
            if (success) Ok(Json.toJson(API.Success[JsObject](data=Json.obj())))
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
