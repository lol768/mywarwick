package controllers.api

import com.google.inject.{Inject, Singleton}
import models.IncomingNotification
import play.api.libs.json._
import play.api.mvc.{Action, Controller}
import services.NotificationService

@Singleton
class IncomingNotificationController @Inject()(
                                                notificationService: NotificationService
                                                ) extends Controller {

  implicit val notificationReads = Json.reads[IncomingNotification]

  def handler = Action(parse.json) { request =>
    request.body.validate[IncomingNotification].map {
      case notification: IncomingNotification =>
        val notificationId: String = notificationService.save(notification.providerId, notification.notificationType, notification.title, notification.text, notification.replaces)
        Ok(Json.toJson(
          Map("status" -> "ok",
            "notificationId" -> notificationId)))
    }.recoverTotal {
      e => BadRequest("Error:" + JsError.toJson(e))
    }
  }
}
