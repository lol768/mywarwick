package controllers.api

import controllers.MyController
import javax.inject.{Inject, Singleton}
import models.API
import models.messaging.DoNotDisturbPeriod
import play.api.libs.json.{Format, JsObject, JsValue, Json}
import play.api.mvc.Action
import services.SecurityService
import services.messaging.DoNotDisturbService
import warwick.core.Logging
import system.AuditLogContext

sealed case class IncomingDoNotDisturbData(enabled: Boolean, doNotDisturb: Option[DoNotDisturbPeriod])

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
        data.doNotDisturb.flatMap(DoNotDisturbPeriod.validate).map { dnd =>
          doNotDisturbService.set(user.usercode, dnd)
          import dnd._
          auditLog('UpdateDoNotDisturb, 'enabled -> true, 'start -> Map('hour -> start.hr, 'min -> start.min), 'end -> Map('hour -> end.hr, 'min -> end.min))
          Ok(Json.toJson(API.Success("ok", "updated")))
        }.getOrElse {
          logger.error(s"Failed to update DoNotDisturb for user ${user.usercode.string}. Bad JSON ${Json.prettyPrint(request.body)}")
          BadRequest(Json.toJson(API.Failure[JsObject]("invalid_data", Seq(API.Error("invalid_data", "do not disturb values were invalid")))))
        }
      } else {
        doNotDisturbService.disable(user.usercode)
        auditLog('UpdateDoNotDisturb, 'enabled -> false)
        Ok(Json.toJson(API.Success("ok", "disabled")))
      }
    }.recoverTotal(jsErr =>
      BadRequest(Json.toJson(API.Failure[JsObject]("bad_request", API.Error.fromJsError(jsErr))))
    )
  }
}
