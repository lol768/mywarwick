package controllers.api

import com.google.inject.Inject
import controllers.BaseController
import play.api.libs.json.Json
import services.SecurityService
import services.messaging.GCMOutputService

class PushNotificationsController @Inject()(
  securityService: SecurityService,
  pushNotificationsService: GCMOutputService
) extends BaseController {

  import securityService._

  def subscribe = RequiredUserAction { request =>
    request.context.user.map { user =>
      request.body.asJson.map { json =>
        val endpoint = (json \ "endpoint").as[String]
        val regId = endpoint.split("/").last
        pushNotificationsService.subscribe(user.usercode, regId) match {
          case true => Created("subscribed to push notifications")
          case false => InternalServerError("oh dear there's been an error")
        }
      }.get
    }.get
  }


  def fetchNotification = UserAction { request =>
    // get the registration
    // get all notifications since last check
    // update last check to now
    request.context.user.map { user =>
      Ok(Json.obj(
        "title" -> s"Hello ${user.name.first.get}",
        "body" -> "You have subscribed to push notifications",
        "icon" -> "/assets/images/notification-icon.png"
      ))
    }.get
  }
}

