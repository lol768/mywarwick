package controllers.api

import javax.inject.Singleton

import com.google.inject.Inject
import controllers.MyController
import models.API
import play.api.libs.json._
import services.{EmailNotificationsPrefService, SecurityService}

@Singleton
class EmailNotificationsPrefController @Inject()(
  security: SecurityService,
  notificationPrefService: EmailNotificationsPrefService
) extends MyController {

  import security._

  def get = RequiredUserAction { request =>
    val user = request.context.user.get

    val data = JsObject(Map(
      "wantsEmails" -> JsBoolean(notificationPrefService.get(user.usercode))
    ))

    Ok(Json.toJson(API.Success(data = data)))
  }

  def update = RequiredUserAction { implicit request =>
    request.context.user.map { user =>
      val newPref = request.body.asJson.flatMap(_.asInstanceOf[JsObject].value.get("wantsEmails")).map(_.as[Boolean])

      newPref.map { pref =>
        auditLog('UpdateEmailsPref, ('newChoice, pref))
        notificationPrefService.set(user.usercode, pref)
        Ok(Json.obj(
          "success" -> true,
          "data" -> JsObject(Map(
            "wantsEmails" -> JsBoolean(pref)
          )),
          "status" -> "ok"
        ))
      }.getOrElse(BadRequest("Must specify new preference value"))

    }.get
  }
}
