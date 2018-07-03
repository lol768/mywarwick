package controllers.api

import controllers.MyController
import javax.inject.{Inject, Singleton}
import models.API
import models.messaging.DoNotDisturbPeriod
import play.api.libs.json._
import play.api.mvc.Action
import services.SecurityService
import services.messaging.DoNotDisturbService

sealed case class IncomingDoNotDisturbData(enabled: Boolean, doNotDisturb: DoNotDisturbPeriod)

private object IncomingDoNotDisturbData {
  implicit val format: Format[IncomingDoNotDisturbData] = Json.format[IncomingDoNotDisturbData]
}

@Singleton
class DoNotDisturbController @Inject()(
  doNotDisturbService: DoNotDisturbService,
  security: SecurityService
) extends MyController {

  import security._

  def index = RequiredUserAction { request =>
    val user = request.context.user.get
    doNotDisturbService.get(user.usercode).map(dnd =>
      Ok(Json.toJson(
        API.Success(data = Json.obj("enabled" -> true) ++ Json.toJson(dnd).as[JsObject])
      ))
    ).getOrElse(
      Ok(Json.toJson(
        API.Success(data = Json.obj("enabled" -> false))
      ))
    )
  }

  def update: Action[JsValue] = RequiredUserAction(parse.json) { implicit request =>
    val user = request.context.user.get
    request.body.validate[IncomingDoNotDisturbData].map { data =>
      if (data.enabled) {
          doNotDisturbService.set(user.usercode, data.doNotDisturb)
          auditLog('UpdateDoNotDisturb, 'enabled -> true, 'start -> data.doNotDisturb.start.format(DoNotDisturbPeriod.dateTimeFormatter), 'end -> data.doNotDisturb.end.format(DoNotDisturbPeriod.dateTimeFormatter))
          Ok(Json.toJson(API.Success("ok", "updated")))
      } else {
        doNotDisturbService.disable(user.usercode)
        auditLog('UpdateDoNotDisturb, 'enabled -> false)
        Ok(Json.toJson(API.Success("ok", "disabled")))
      }
    }.recoverTotal(jsErr => {
      logger.error(s"Error validating Do Not Disturb JSON. $jsErr")
      BadRequest(Json.toJson(API.Failure[JsObject]("bad_request", API.Error.fromJsError(jsErr))))
    })
  }
}
