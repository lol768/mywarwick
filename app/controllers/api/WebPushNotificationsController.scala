package controllers.api

import javax.inject.Singleton

import com.google.inject.Inject
import controllers.BaseController
import models.Platform.WebPush
import models.messaging.Subscription
import play.api.libs.json.Json
import services.messaging.FetchNotificationsService
import services.{PushRegistrationService, SecurityService}

@Singleton
class WebPushNotificationsController @Inject()(
  securityService: SecurityService,
  pushRegistrationService: PushRegistrationService,
  fetchNotificationsService: FetchNotificationsService
) extends BaseController {

  import securityService._

  def subscribe = RequiredUserAction { implicit request =>
    val user = request.context.user.get // RequiredUserAction

    val maybeSubscription = request.body.asJson.flatMap(_.validate[Subscription].asOpt)

    maybeSubscription.map { subscription =>
      val token = Json.toJson(subscription).toString()
      val userAgent = request.request.headers.get("user-agent").getOrElse("Unknown")

      pushRegistrationService.save(user.usercode, WebPush, token, userAgent) match {
        case true =>
          auditLog('CreateWebPushSubscription, 'endpoint -> subscription.endpoint)

          Created(Json.obj(
            "success" -> true,
            "status" -> "ok"
          ))
        case false =>
          InternalServerError(Json.obj(
            "success" -> false,
            "status" -> "internal server error"
          ))
      }
    }.getOrElse {
      BadRequest(Json.obj(
        "success" -> false,
        "status" -> "bad request"
      ))
    }
  }

}

