package controllers.api

import com.google.inject.Inject
import controllers.BaseController
import models.Platform.Google
import play.api.libs.json.JsValue
import services.messaging.FetchNotificationsService
import services.{PushRegistrationService, SecurityService}

class GCMPushNotificationsController @Inject()(
  securityService: SecurityService,
  pushRegistrationService: PushRegistrationService,
  fetchNotificationsService: FetchNotificationsService
) extends BaseController {

  import securityService._

  def subscribe = RequiredUserAction { request =>
    request.context.user.map { user =>
      request.body.asJson.map { json =>
        val endpoint = (json \ "endpoint").as[String]
        val token = endpoint.split("/").last
        pushRegistrationService.save(user.usercode, Google, token) match {
          case true => Created("subscribed to push notifications")
          case false => InternalServerError("oh dear there's been an error")
        }
      }.get
    }.get
  }

  def fetchPushNotifications = RequiredUserAction { request =>
    request.body.asJson.map { json =>
      val endpoint = (json \ "endpoint").as[String]
      val token = endpoint.split("/").last
      val notifications: JsValue = fetchNotificationsService.fetchPushNotifications(token)
      Ok(notifications)
    }.get
  }
}

