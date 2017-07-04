package controllers.api

import javax.inject.Singleton

import com.google.inject.Inject
import controllers.BaseController
import models.{API, Audience}
import play.api.libs.json._
import services.{SecurityService, UserNewsOptInService}

@Singleton
class UserNewsOptInController @Inject()(
  security: SecurityService,
  userNewsOptInService: UserNewsOptInService
) extends BaseController {

  import security._

  def list = RequiredUserAction { request =>
    val user = request.context.user.get // RequiredUserAction

    val options = userNewsOptInService.get(user.usercode)

    val data = JsObject(Map(
      "options" -> JsObject(Map(
        Audience.LocationOptIn.optInType -> JsArray(Audience.LocationOptIn.values.map(option => JsObject(Map(
          "value" -> JsString(option.value),
          "description" -> JsString(option.description)
        ))))
      )),
      "selected" -> JsObject(options.groupBy(_.optInType).map { case (optInType, values) =>
        optInType -> JsArray(values.map(v => JsString(v.optInValue)))
      }.toSeq)
    ))

    Ok(Json.toJson(API.Success(data = data)))
  }

  def update = RequiredUserAction { implicit request =>
    request.context.user.map { user =>
      request.body.asJson.collect { case o: JsObject => o }.map(_.value.mapValues {
        case jsArray: JsArray => jsArray.value.collect { case s: JsString => s.value }
        case _ => Seq()
      }).map(_.flatMap { case (optInType, stringValues) => optInType match {
          case Audience.LocationOptIn.optInType =>
            Some(optInType -> stringValues.flatMap(Audience.LocationOptIn.fromValue))
          case _ => None
        }
      }).map(_.map { case (optInType, optInValues) =>
        userNewsOptInService.set(user.usercode, optInType, optInValues)
        (optInType, optInType)
      }).map { result =>
        auditLog('SetOptIn, 'result -> result)
        Ok(Json.toJson(API.Success("ok", "saved")))
      }.getOrElse(BadRequest(Json.toJson(API.Failure[JsObject]("bad request", Seq(API.Error("invalid-body", "Body must be JSON-formatted"))))))
    }.get // RequiredUserAction
  }
}
