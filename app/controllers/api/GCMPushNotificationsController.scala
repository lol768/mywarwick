package controllers.api

import javax.inject.Singleton

import com.google.inject.Inject
import controllers.BaseController
import models.Platform.Google
import play.api.libs.json.JsValue
import services.messaging.FetchNotificationsService
import services.{PushRegistrationService, SecurityService}

@Singleton
class GCMPushNotificationsController @Inject()(
  securityService: SecurityService,
  pushRegistrationService: PushRegistrationService,
  fetchNotificationsService: FetchNotificationsService
) extends BaseController {

  import securityService._

  def subscribe = RequiredUserAction { implicit request =>
    request.context.user.map { user =>
      request.body.asJson.map { json =>
        val endpoint = (json \ "endpoint").as[String]
        val token = endpoint.split("/").last
        pushRegistrationService.save(user.usercode, Google, token) match {
          case true =>
            auditLog('CreateGCMSubscription, 'token -> token)

            Created("subscribed to push notifications")
          case false =>
            InternalServerError("oh dear there's been an error")
        }
      }.get
    }.get
  }

  def fetchPushNotifications = APIAction { request =>
    request.body.asJson.map { json =>
      val endpoint = (json \ "endpoint").as[String]
      val token = endpoint.split("/").last
      val notifications: JsValue = fetchNotificationsService.fetchPushNotifications(token)
      Ok(notifications)
    }.get
  }
}

