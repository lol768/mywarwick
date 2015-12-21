package controllers.api

import com.google.inject.Inject
import controllers.BaseController
import play.api.libs.json.Json
import services.SecurityService
import services.messaging.PushNotificationsService

class PushNotificationsController @Inject()(
  securityService: SecurityService,
  pushNotificationsService: PushNotificationsService
) extends BaseController {

  import securityService._

  def subscribe = RequiredUserAction { request =>
    request.context.user.map { user =>
      request.headers.get("User-Agent").map { agent =>
        request.body.asJson.map { json =>
          val endpoint = (json \ "endpoint").as[String]
          val regId = endpoint.split("/").last
          pushNotificationsService.subscribe(user.usercode.string, agent, regId) match {
            case true => Created("subscribed to push notifications")
            case false => InternalServerError("oh dear there's been an error")
          }
        }.get
      }.get
    }.get
  }


  def fetchNotification = UserAction { request =>
    request.context.user.map { user =>
      Ok(Json.obj(
        "title" -> s"Hello ${user.name.first.get}",
        "body" -> "You have subscribed to push notifications",
        "icon" -> "/assets/images/notification-icon.png"
      ))
    }.get
  }
}

