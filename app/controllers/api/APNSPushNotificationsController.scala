package controllers.api

import javax.inject.Singleton

import com.google.inject.Inject
import controllers.BaseController
import models.Platform.Apple
import play.api.libs.json.JsObject
import services.{PushRegistrationService, SecurityService}

@Singleton
class APNSPushNotificationsController @Inject()(
  securityService: SecurityService,
  pushRegistrationService: PushRegistrationService
) extends BaseController {

  import securityService._

  def subscribe = RequiredUserAction { implicit request =>
    val deviceToken = request.body.asJson.flatMap(_.asInstanceOf[JsObject].value.get("deviceToken")).map(_.as[String])
    val userAgent = request.request.headers.get("user-agent").getOrElse("Unknown")

    deviceToken.map { token =>
      val registered = pushRegistrationService.save(request.context.user.get.usercode, Apple, token, userAgent)

      if (registered) {
        auditLog('CreateAPNSRegistration, 'token -> token)

        Ok("Registered for push notifications")
      } else {
        InternalServerError("Unable to register for push notifications")
      }
    }.getOrElse(BadRequest("Must specify deviceToken"))
  }

}
