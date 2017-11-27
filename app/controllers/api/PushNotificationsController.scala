package controllers.api

import javax.inject.Singleton

import com.google.inject.Inject
import controllers.MyController
import play.api.libs.json.JsObject
import play.api.mvc.Action
import services.PushRegistrationService

@Singleton
class PushNotificationsController @Inject()(
  pushRegistrationService: PushRegistrationService
) extends MyController {

  def unsubscribe = Action { implicit request =>
    val deviceToken = request.body.asJson.flatMap(_.asInstanceOf[JsObject].value.get("deviceToken")).map(_.as[String])

    deviceToken.map { token =>
      auditLog('DeletePushRegistration, 'token -> token)

      pushRegistrationService.remove(token)

      Ok("Registration removed")
    }.getOrElse(BadRequest("Must specify deviceToken"))
  }

}

