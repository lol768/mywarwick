package controllers.api

import com.google.inject.Inject
import models.IncomingNotification
import play.api.libs.json._
import play.api.mvc.{Action, Controller}
import services.NotificationService

class IncomingNotificationController @Inject()(
  notificationService: NotificationService
) extends Controller {

  implicit val dateReads = Reads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")

  implicit val notificationReads = Json.reads[IncomingNotification]

  def handler = Action(parse.json) { request =>
    request.body.validate[IncomingNotification].map { notification =>
      val notificationId: String = notificationService.save(notification)
      Ok(Json.toJson(
        Map("status" -> "ok",
          "notificationId" -> notificationId)))
    }.recoverTotal {
      e => BadRequest("Error:" + JsError.toJson(e))
    }
  }
}
