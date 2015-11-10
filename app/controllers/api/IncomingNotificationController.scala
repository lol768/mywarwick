package controllers.api

import com.google.inject.Inject
import models.IncomingActivity
import play.api.libs.json._
import play.api.mvc.{Action, Controller}
import services.ActivityService

class IncomingNotificationController @Inject()(
  activityService: ActivityService
) extends Controller {

  implicit val dateReads = Reads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")

  implicit val activityReads = Json.reads[IncomingActivity]

  def handler = Action(parse.json) { request =>
    request.body.validate[IncomingActivity].map { activity =>
      val activityId: String = activityService.save(activity, shouldNotify = true)
      Ok(Json.toJson(
        Map("status" -> "ok",
          "activityId" -> activityId)))
    }.recoverTotal {
      e => BadRequest("Error:" + JsError.toJson(e))
    }
  }
}
